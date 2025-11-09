package com.asyncsite.iap.gateway.adapter.in.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple Server Notification V2 - Transaction Data
 *
 * <p>알림에 포함된 거래 정보입니다.
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreservernotifications/data
 *
 * <pre>
 * {
 *   "appAppleId": 123456789,
 *   "bundleId": "com.asyncsite.querydaily",
 *   "bundleVersion": "1.0.0",
 *   "environment": "Production",
 *   "signedTransactionInfo": "eyJhbGciOiJFUzI1NiIsIng1YyI6W... (JWT)"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleTransactionData {

    /**
     * App Apple ID (숫자)
     */
    private Long appAppleId;

    /**
     * Bundle ID (예: com.asyncsite.querydaily)
     */
    private String bundleId;

    /**
     * Bundle Version (예: 1.0.0)
     */
    private String bundleVersion;

    /**
     * 환경
     *
     * <ul>
     *   <li>Sandbox: 테스트 환경</li>
     *   <li>Production: 프로덕션 환경</li>
     * </ul>
     */
    private String environment;

    /**
     * 거래 정보 (JWT)
     *
     * <p>디코딩하면 {@link AppleTransactionInfo}를 얻을 수 있습니다.
     */
    private String signedTransactionInfo;
}
