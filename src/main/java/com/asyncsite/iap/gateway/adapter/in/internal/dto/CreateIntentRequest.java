package com.asyncsite.iap.gateway.adapter.in.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIntentRequest {

    @NotBlank(message = "userEmail is required")
    @Email(message = "Invalid email format")
    private String userEmail;  // QueryDaily Mobile에서 JWT 검증 후 전달

    @NotBlank(message = "productId is required")
    private String productId;  // insight_1000_points
}
