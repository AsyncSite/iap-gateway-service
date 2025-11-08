package com.asyncsite.iap.gateway.domain.intent;

/**
 * IAP Intent 상태
 */
public enum IAPIntentStatus {
    /**
     * 생성됨, 결제 대기 중
     */
    PENDING,

    /**
     * 영수증 검증 완료, Transaction 생성됨
     */
    VERIFIED,

    /**
     * 만료됨 (1시간 초과)
     */
    EXPIRED,

    /**
     * 검증 실패
     */
    FAILED
}
