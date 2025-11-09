package com.asyncsite.iap.gateway.adapter.in.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple JWS Transaction Info - Decoded
 *
 * <p>signedTransactionInfo를 디코딩한 후의 거래 정보입니다.
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreserverapi/jwstransactiondecodedpayload
 *
 * <pre>
 * {
 *   "originalTransactionId": "1000000123456789",
 *   "transactionId": "1000000123456789",
 *   "productId": "com.asyncsite.querydaily.insight_100",
 *   "purchaseDate": 1699876543000,
 *   "originalPurchaseDate": 1699876543000,
 *   "quantity": 1,
 *   "type": "Consumable",
 *   "appAccountToken": "iap_intent_1699876543000_abc123",
 *   "inAppOwnershipType": "PURCHASED",
 *   "signedDate": 1699876543000,
 *   "environment": "Production"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleTransactionInfo {

    /**
     * 원본 거래 ID (최초 구매 시의 거래 ID)
     */
    private String originalTransactionId;

    /**
     * 거래 ID (현재 거래의 고유 ID)
     */
    private String transactionId;

    /**
     * 상품 ID (예: com.asyncsite.querydaily.insight_100)
     */
    private String productId;

    /**
     * 구매 날짜 (Unix timestamp, milliseconds)
     */
    private Long purchaseDate;

    /**
     * 원본 구매 날짜 (Unix timestamp, milliseconds)
     */
    private Long originalPurchaseDate;

    /**
     * 구매 수량
     */
    private Integer quantity;

    /**
     * 상품 타입
     *
     * <ul>
     *   <li>Consumable: 소비형 (Insight)</li>
     *   <li>Non-Consumable: 비소비형</li>
     *   <li>Auto-Renewable Subscription: 자동 갱신 구독</li>
     *   <li>Non-Renewing Subscription: 비갱신 구독</li>
     * </ul>
     */
    private String type;

    /**
     * App Account Token
     *
     * <p>⭐ 중요: Intent ID가 포함된 값입니다.
     * <p>클라이언트에서 구매 시 ApplicationUsername에 설정한 값입니다.
     * <p>예: "iap_intent_1699876543000_abc123"
     */
    private String appAccountToken;

    /**
     * 소유권 타입
     *
     * <ul>
     *   <li>PURCHASED: 구매</li>
     *   <li>FAMILY_SHARED: 가족 공유</li>
     * </ul>
     */
    private String inAppOwnershipType;

    /**
     * 서명 날짜 (Unix timestamp, milliseconds)
     */
    private Long signedDate;

    /**
     * 환경 (Sandbox / Production)
     */
    private String environment;

    // =================================================================
    // Business Methods
    // =================================================================

    /**
     * 일회성 구매(소비형) 상품인지 확인
     */
    public boolean isConsumable() {
        return "Consumable".equalsIgnoreCase(type);
    }

    /**
     * appAccountToken에서 Intent ID 추출
     *
     * <p>예: "iap_intent_1699876543000_abc123" → "iap_intent_1699876543000_abc123"
     */
    public String extractIntentId() {
        return appAccountToken;
    }
}
