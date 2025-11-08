package com.asyncsite.iap.gateway.adapter.out.persistence;

import com.asyncsite.iap.gateway.domain.intent.*;
import org.springframework.stereotype.Component;

/**
 * IAPIntent ↔ IAPIntentEntity 매퍼
 */
@Component
public class IAPIntentMapper {

    public IAPIntentEntity toEntity(IAPIntent domain) {
        return IAPIntentEntity.builder()
            .intentId(domain.getIntentId().getValue())
            .userEmail(domain.getUserEmail().getValue())
            .productId(domain.getProductId().getValue())
            .status(domain.getStatus())
            .transactionId(domain.getTransactionId())
            .errorCode(domain.getErrorCode())
            .errorMessage(domain.getErrorMessage())
            .createdAt(domain.getCreatedAt())
            .expiresAt(domain.getExpiresAt())
            .verifiedAt(domain.getVerifiedAt())
            .build();
    }

    public IAPIntent toDomain(IAPIntentEntity entity) {
        return new IAPIntent(
            IAPIntentId.of(entity.getIntentId()),
            UserEmail.of(entity.getUserEmail()),
            ProductId.of(entity.getProductId()),
            entity.getStatus(),
            entity.getTransactionId(),
            entity.getErrorCode(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getExpiresAt(),
            entity.getVerifiedAt()
        );
    }
}
