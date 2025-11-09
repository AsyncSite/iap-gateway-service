package com.asyncsite.iap.gateway.adapter.out.client;

import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationRequest;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Payment Core Internal API Client
 *
 * Payment Core의 IAP 검증 API를 호출합니다.
 */
@FeignClient(
    name = "payment-core",
    url = "${payment-core.url:http://localhost:6082}",
    configuration = PaymentCoreFeignConfig.class
)
public interface PaymentCoreClient {

    /**
     * IAP 영수증 검증
     *
     * @param request 검증 요청 (intentId, userEmail, productId, platform, receiptData/purchaseToken)
     * @return 검증 결과
     */
    @PostMapping("/internal/api/v1/iap/verify")
    IAPVerificationResponse verifyReceipt(@RequestBody IAPVerificationRequest request);
}
