package com.asyncsite.iap.gateway.adapter.out.client.dto;

import com.asyncsite.iap.gateway.domain.intent.Platform;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Payment Core IAP 검증 요청 DTO
 *
 * <p>Payment Core Internal API: POST /internal/api/v1/iap/verify
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IAPVerificationRequest {

    /**
     * Intent ID (IAP Gateway에서 생성한 고유 ID)
     */
    private String intentId;

    /**
     * 사용자 이메일 (Payment Core에서는 userId로 받음)
     */
    @JsonProperty("userId")
    private String userEmail;

    /**
     * 상품 ID (예: com.asyncsite.querydaily.insights.500)
     */
    private String productId;

    /**
     * 플랫폼 (ANDROID / IOS)
     */
    private Platform platform;

    /**
     * iOS: Base64 encoded receipt (Optional for Server Notification)
     */
    private String receiptData;

    /**
     * Android: purchaseToken (RTDN에서 제공)
     */
    private String purchaseToken;
}
