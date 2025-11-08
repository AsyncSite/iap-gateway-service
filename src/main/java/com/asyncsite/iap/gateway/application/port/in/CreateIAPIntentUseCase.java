package com.asyncsite.iap.gateway.application.port.in;

import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.ProductId;
import com.asyncsite.iap.gateway.domain.intent.UserEmail;

/**
 * IAP Intent 생성 Use Case
 */
public interface CreateIAPIntentUseCase {

    /**
     * 새로운 IAP Intent 생성
     *
     * @param userEmail JWT 검증된 사용자 이메일
     * @param productId 구매할 상품 ID
     * @return 생성된 IAPIntent
     */
    IAPIntent createIntent(UserEmail userEmail, ProductId productId);
}
