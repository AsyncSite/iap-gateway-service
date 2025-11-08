package com.asyncsite.iap.gateway.adapter.in.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateIntentResponse {
    private String intentId;
    private String userEmail;
    private String productId;
    private String status;
    private String createdAt;
    private String expiresAt;
}
