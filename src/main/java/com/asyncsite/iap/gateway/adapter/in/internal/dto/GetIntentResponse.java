package com.asyncsite.iap.gateway.adapter.in.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetIntentResponse {
    private String intentId;
    private String userEmail;
    private String productId;
    private String status;
    private String transactionId;  // VERIFIED 상태일 때만
    private String errorCode;      // FAILED 상태일 때만
    private String errorMessage;   // FAILED 상태일 때만
    private String createdAt;
    private String verifiedAt;
}
