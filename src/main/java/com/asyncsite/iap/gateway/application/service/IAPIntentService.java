package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.application.port.in.CreateIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.in.GetIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IAPIntentService implements CreateIAPIntentUseCase, GetIAPIntentUseCase {

    private final IAPIntentRepository intentRepository;

    @Override
    @Transactional
    public IAPIntent createIntent(UserEmail userEmail, ProductId productId) {
        log.info("Creating IAP Intent: userEmail={}, productId={}",
                 userEmail.getValue(), productId.getValue());

        // 1. Intent 생성
        IAPIntent intent = IAPIntent.create(userEmail, productId);

        // 2. 저장
        IAPIntent saved = intentRepository.save(intent);

        log.info("IAP Intent created: intentId={}", saved.getIntentId().getValue());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public IAPIntent getIntent(IAPIntentId intentId) {
        log.debug("Getting IAP Intent: intentId={}", intentId.getValue());

        IAPIntent intent = intentRepository.findById(intentId)
            .orElseThrow(() -> new IntentNotFoundException(intentId.getValue()));

        // 만료 체크
        if (intent.isExpired() && intent.getStatus() == IAPIntentStatus.PENDING) {
            throw new IntentExpiredException(intentId.getValue());
        }

        return intent;
    }
}
