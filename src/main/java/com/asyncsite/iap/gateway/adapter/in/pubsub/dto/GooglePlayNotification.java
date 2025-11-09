package com.asyncsite.iap.gateway.adapter.in.pubsub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Google Play Real-time Developer Notification DTO
 *
 * 참고: https://developer.android.com/google/play/billing/rtdn-reference
 *
 * Google Play는 4가지 Notification 타입 중 정확히 하나를 전송:
 * - oneTimeProductNotification (일회성 구매)
 * - subscriptionNotification (구독)
 * - voidedPurchaseNotification (환불/취소)
 * - testNotification (테스트)
 */
@Getter
@Setter
public class GooglePlayNotification {

    private String version;
    private String packageName;

    @JsonProperty("eventTimeMillis")
    private String eventTimeMillis;  // Google sends as String, not Long

    @JsonProperty("oneTimeProductNotification")
    private OneTimeProductNotification oneTimeProductNotification;

    @JsonProperty("subscriptionNotification")
    private SubscriptionNotification subscriptionNotification;

    @JsonProperty("voidedPurchaseNotification")
    private VoidedPurchaseNotification voidedPurchaseNotification;

    @JsonProperty("testNotification")
    private TestNotification testNotification;

    /**
     * 일회성 구매 알림
     */
    @Getter
    @Setter
    public static class OneTimeProductNotification {
        private String version;
        private Integer notificationType;  // 1=ONE_TIME_PRODUCT_PURCHASED, 2=ONE_TIME_PRODUCT_CANCELED
        private String purchaseToken;
        private String sku;  // productId
    }

    /**
     * 구독 알림 (Phase 4에서는 미사용, 향후 구독 지원 시 구현)
     */
    @Getter
    @Setter
    public static class SubscriptionNotification {
        private String version;
        private Integer notificationType;
        private String purchaseToken;
        private String subscriptionId;
    }

    /**
     * 환불/취소 알림
     */
    @Getter
    @Setter
    public static class VoidedPurchaseNotification {
        private String purchaseToken;
        private String orderId;
        private Integer productType;  // 0=SUBSCRIPTION, 1=ONE_TIME
        private Long refundType;
    }

    /**
     * 테스트 알림 (Google Play Console 연동 테스트용)
     */
    @Getter
    @Setter
    public static class TestNotification {
        private String version;
    }

    // ===== Helper Methods =====

    /**
     * 테스트 알림 여부 확인
     */
    public boolean isTestNotification() {
        return testNotification != null;
    }

    /**
     * notificationType 해석 - PURCHASED 이벤트 확인
     *
     * @return 구매 이벤트면 true
     */
    public boolean isPurchased() {
        return oneTimeProductNotification != null
            && oneTimeProductNotification.getNotificationType() != null
            && oneTimeProductNotification.getNotificationType() == 1;
    }

    /**
     * 일회성 구매 알림 여부 확인
     */
    public boolean isOneTimeProductNotification() {
        return oneTimeProductNotification != null;
    }

    /**
     * 환불/취소 알림 여부 확인
     */
    public boolean isVoidedPurchase() {
        return voidedPurchaseNotification != null;
    }

    /**
     * purchaseToken 추출
     *
     * @return purchaseToken
     */
    public String getPurchaseToken() {
        if (oneTimeProductNotification != null) {
            return oneTimeProductNotification.getPurchaseToken();
        }
        return null;
    }

    /**
     * productId (sku) 추출
     *
     * @return productId
     */
    public String getProductId() {
        if (oneTimeProductNotification != null) {
            return oneTimeProductNotification.getSku();
        }
        return null;
    }

    /**
     * Notification 타입 확인 (디버깅용)
     */
    public String getNotificationType() {
        if (testNotification != null) return "TEST";
        if (oneTimeProductNotification != null) return "ONE_TIME_PRODUCT";
        if (subscriptionNotification != null) return "SUBSCRIPTION";
        if (voidedPurchaseNotification != null) return "VOIDED_PURCHASE";
        return "UNKNOWN";
    }
}
