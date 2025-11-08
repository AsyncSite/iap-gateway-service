package com.asyncsite.iap.gateway.application.port.in;

import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentId;

/**
 * IAP Intent 조회 Use Case
 */
public interface GetIAPIntentUseCase {

    /**
     * Intent 조회
     *
     * @param intentId Intent ID
     * @return IAPIntent
     * @throws com.asyncsite.iap.gateway.domain.intent.IntentNotFoundException Intent가 없으면
     */
    IAPIntent getIntent(IAPIntentId intentId);
}
