package com.asyncsite.iap.gateway.domain.intent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * IAP Intent Aggregate Root
 *
 * 역할:
 * 1. userId 저장소 (JWT 검증된 사용자 정보)
 * 2. appAccountToken 생성 (intentId)
 * 3. 중복 방지 (1차 방어)
 * 4. 폴링 결과 확인
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IAPIntent {

    private IAPIntentId intentId;
    private UserEmail userEmail;
    private ProductId productId;
    private IAPIntentStatus status;
    private String transactionId;  // Payment Core Transaction ID (완료 후)
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant verifiedAt;

    // All-args constructor for Mapper (package-private for persistence adapter)
    public IAPIntent(
        IAPIntentId intentId,
        UserEmail userEmail,
        ProductId productId,
        IAPIntentStatus status,
        String transactionId,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant expiresAt,
        Instant verifiedAt
    ) {
        this.intentId = intentId;
        this.userEmail = userEmail;
        this.productId = productId;
        this.status = status;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    // ===== Factory Method =====

    /**
     * 새로운 IAP Intent 생성
     *
     * @param userEmail JWT 검증된 사용자 이메일 (신뢰된 정보!)
     * @param productId 구매할 상품 ID
     * @return 새로운 IAPIntent
     */
    public static IAPIntent create(UserEmail userEmail, ProductId productId) {
        IAPIntentId intentId = IAPIntentId.generate();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);  // 1시간 후

        return new IAPIntent(
            intentId,
            userEmail,
            productId,
            IAPIntentStatus.PENDING,
            null,  // transactionId
            null,  // errorCode
            null,  // errorMessage
            now,
            expiresAt,
            null   // verifiedAt
        );
    }

    // ===== Domain Methods =====

    /**
     * 검증 완료 처리
     *
     * @param transactionId Payment Core Transaction ID
     */
    public void markAsVerified(String transactionId) {
        if (this.status == IAPIntentStatus.VERIFIED) {
            throw new IllegalStateException(
                "Intent already verified: " + this.intentId.getValue()
            );
        }

        this.status = IAPIntentStatus.VERIFIED;
        this.transactionId = transactionId;
        this.verifiedAt = Instant.now();
    }

    /**
     * 실패 처리
     *
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = IAPIntentStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 만료 여부 확인
     *
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * 만료 처리
     */
    public void markAsExpired() {
        if (this.status == IAPIntentStatus.PENDING && isExpired()) {
            this.status = IAPIntentStatus.EXPIRED;
        }
    }

    /**
     * 검증 가능 상태인지 확인
     *
     * @return PENDING 상태이고 만료 안됐으면 true
     */
    public boolean canVerify() {
        return this.status == IAPIntentStatus.PENDING && !isExpired();
    }
}
