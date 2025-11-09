package com.asyncsite.iap.gateway.adapter.in.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple Server Notification V2 - Decoded Payload
 *
 * <p>signedPayload를 디코딩한 후의 데이터 구조입니다.
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreservernotifications/responsebodyv2decodedpayload
 *
 * <pre>
 * {
 *   "notificationType": "CONSUMPTION_REQUEST",
 *   "subtype": "INITIAL_BUY",
 *   "notificationUUID": "12345678-1234-1234-1234-123456789012",
 *   "version": "2.0",
 *   "signedDate": 1699876543000,
 *   "data": {
 *     "appAppleId": 123456789,
 *     "bundleId": "com.asyncsite.querydaily",
 *     "bundleVersion": "1.0.0",
 *     "environment": "Production",
 *     "signedTransactionInfo": "eyJhbGciOiJFUzI1NiIsIng1YyI6W... (JWT)"
 *   }
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleNotificationData {

    /**
     * 알림 타입
     *
     * <p>주요 타입:
     * <ul>
     *   <li>CONSUMPTION_REQUEST: 구매 완료 (일회성 구매)</li>
     *   <li>REFUND: 환불</li>
     *   <li>DID_RENEW: 구독 갱신</li>
     *   <li>EXPIRED: 구독 만료</li>
     *   <li>GRACE_PERIOD_EXPIRED: 유예 기간 만료</li>
     * </ul>
     */
    private String notificationType;

    /**
     * 알림 서브타입
     *
     * <p>예: INITIAL_BUY, RESUBSCRIBE, UPGRADE, DOWNGRADE
     */
    private String subtype;

    /**
     * 알림 고유 ID (UUID)
     */
    private String notificationUUID;

    /**
     * 알림 버전 (예: "2.0")
     */
    private String version;

    /**
     * 알림 발송 시각 (Unix timestamp, milliseconds)
     */
    private Long signedDate;

    /**
     * 거래 데이터
     */
    private AppleTransactionData data;
}
