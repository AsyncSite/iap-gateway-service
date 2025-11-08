package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.application.port.in.CreateIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.in.GetIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IAPIntentService implements CreateIAPIntentUseCase, GetIAPIntentUseCase {

    private final IAPIntentRepository intentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "iap:intent:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    @Transactional
    public IAPIntent createIntent(UserEmail userEmail, ProductId productId) {
        log.info("Creating IAP Intent: userEmail={}, productId={}",
                 userEmail.getValue(), productId.getValue());

        // 1. Intent 생성
        IAPIntent intent = IAPIntent.create(userEmail, productId);

        // 2. 저장
        IAPIntent saved = intentRepository.save(intent);

        // 3. Redis 캐싱 (10분 TTL)
        String cacheKey = CACHE_KEY_PREFIX + saved.getIntentId().getValue();
        redisTemplate.opsForValue().set(cacheKey, saved, CACHE_TTL);

        log.info("IAP Intent created and cached: intentId={}, ttl={}",
                 saved.getIntentId().getValue(), CACHE_TTL);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public IAPIntent getIntent(IAPIntentId intentId) {
        log.debug("Getting IAP Intent: intentId={}", intentId.getValue());

        // 1. Redis 캐시 확인
        String cacheKey = CACHE_KEY_PREFIX + intentId.getValue();
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.debug("Cache hit: intentId={}", intentId.getValue());
            IAPIntent cachedIntent = (IAPIntent) cached;

            // 캐시된 데이터도 만료 체크
            if (cachedIntent.isExpired() && cachedIntent.getStatus() == IAPIntentStatus.PENDING) {
                // 만료된 캐시 삭제
                redisTemplate.delete(cacheKey);
                throw new IntentExpiredException(intentId.getValue());
            }

            return cachedIntent;
        }

        // 2. Redis miss → MySQL 조회
        log.debug("Cache miss: intentId={}", intentId.getValue());
        IAPIntent intent = intentRepository.findById(intentId)
            .orElseThrow(() -> new IntentNotFoundException(intentId.getValue()));

        // 3. 만료 체크
        if (intent.isExpired() && intent.getStatus() == IAPIntentStatus.PENDING) {
            throw new IntentExpiredException(intentId.getValue());
        }

        // 4. Redis에 캐싱
        redisTemplate.opsForValue().set(cacheKey, intent, CACHE_TTL);
        log.debug("Intent cached: intentId={}", intentId.getValue());

        return intent;
    }
}
