package com.asyncsite.iap.gateway.application.port.out;

import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentId;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentStatus;
import com.asyncsite.iap.gateway.domain.intent.ProductId;
import com.asyncsite.iap.gateway.domain.intent.UserEmail;

import java.util.List;
import java.util.Optional;

/**
 * IAP Intent Repository Port (Output Port)
 */
public interface IAPIntentRepository {

    IAPIntent save(IAPIntent intent);

    Optional<IAPIntent> findById(IAPIntentId intentId);

    List<IAPIntent> findByUserEmail(UserEmail userEmail);

    /**
     * ProductId와 Status로 가장 최근 Intent 조회 (Phase 4 Google Play 알림 처리용)
     *
     * @param productId 상품 ID
     * @param status Intent 상태
     * @return 가장 최근 Intent
     */
    Optional<IAPIntent> findTopByProductIdAndStatusOrderByCreatedAtDesc(ProductId productId, IAPIntentStatus status);

    int expireOldIntents();
}
