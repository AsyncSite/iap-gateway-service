package com.asyncsite.iap.gateway.adapter.out.persistence;

import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentId;
import com.asyncsite.iap.gateway.domain.intent.UserEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IAPIntentPersistenceAdapter implements IAPIntentRepository {

    private final IAPIntentJpaRepository jpaRepository;
    private final IAPIntentMapper mapper;

    @Override
    public IAPIntent save(IAPIntent intent) {
        IAPIntentEntity entity = mapper.toEntity(intent);
        IAPIntentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<IAPIntent> findById(IAPIntentId intentId) {
        return jpaRepository.findByIntentId(intentId.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public List<IAPIntent> findByUserEmail(UserEmail userEmail) {
        return jpaRepository.findByUserEmailOrderByCreatedAtDesc(userEmail.getValue())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public int expireOldIntents() {
        return jpaRepository.expireOldIntents(Instant.now());
    }
}
