package com.asyncsite.iap.gateway.adapter.in.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple App Store Server Notifications V2 - Root Payload
 *
 * <p>Apple에서 전송하는 Webhook의 최상위 구조입니다.
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreservernotifications/responsebodyv2
 *
 * <pre>
 * {
 *   "signedPayload": "eyJhbGciOiJFUzI1NiIsIng1YyI6W... (JWT)"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleNotificationPayload {

    /**
     * JWS (JSON Web Signature) 형식의 JWT
     *
     * <p>디코딩하면 {@link AppleNotificationData}를 얻을 수 있습니다.
     */
    private String signedPayload;
}
