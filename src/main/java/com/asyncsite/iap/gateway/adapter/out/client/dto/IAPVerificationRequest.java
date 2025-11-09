package com.asyncsite.iap.gateway.adapter.out.client.dto;

import com.asyncsite.iap.gateway.domain.intent.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Payment Core IAP 검증 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IAPVerificationRequest {

    private String intentId;
    private String userEmail;
    private String productId;
    private Platform platform;

    // iOS: Base64 encoded receipt
    private String receiptData;

    // Android: purchaseToken
    private String purchaseToken;
}
