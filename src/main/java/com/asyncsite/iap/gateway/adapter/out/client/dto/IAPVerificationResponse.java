package com.asyncsite.iap.gateway.adapter.out.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Core IAP 검증 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IAPVerificationResponse {

    private boolean success;
    private String intentId;
    private String platformTransactionId;
    private String productId;
    private Integer insightAmount;
    private String currency;
    private BigDecimal amount;
    private Instant verifiedAt;
    private String errorMessage;
}
