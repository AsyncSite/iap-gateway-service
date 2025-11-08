package com.asyncsite.iap.gateway.domain.intent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IAPIntent 도메인 모델 테스트")
class IAPIntentTest {

    @Test
    @DisplayName("create() - 새로운 Intent를 생성한다")
    void create_ShouldCreateNewIntent() {
        // given
        UserEmail userEmail = UserEmail.of("test@example.com");
        ProductId productId = ProductId.of("insight_1000_points");

        // when
        IAPIntent intent = IAPIntent.create(userEmail, productId);

        // then
        assertThat(intent).isNotNull();
        assertThat(intent.getIntentId()).isNotNull();
        assertThat(intent.getIntentId().getValue()).startsWith("iap_intent_");
        assertThat(intent.getUserEmail()).isEqualTo(userEmail);
        assertThat(intent.getProductId()).isEqualTo(productId);
        assertThat(intent.getStatus()).isEqualTo(IAPIntentStatus.PENDING);
        assertThat(intent.getTransactionId()).isNull();
        assertThat(intent.getErrorCode()).isNull();
        assertThat(intent.getErrorMessage()).isNull();
        assertThat(intent.getCreatedAt()).isNotNull();
        assertThat(intent.getExpiresAt()).isNotNull();
        assertThat(intent.getVerifiedAt()).isNull();
    }

    @Test
    @DisplayName("create() - 만료 시간은 생성 시간 + 1시간이다")
    void create_ShouldSetExpireTimeOneHourLater() {
        // given
        UserEmail userEmail = UserEmail.of("test@example.com");
        ProductId productId = ProductId.of("insight_1000_points");

        // when
        IAPIntent intent = IAPIntent.create(userEmail, productId);

        // then
        long expectedExpireTime = intent.getCreatedAt().getEpochSecond() + 3600;
        long actualExpireTime = intent.getExpiresAt().getEpochSecond();
        assertThat(actualExpireTime).isCloseTo(expectedExpireTime, within(1L));
    }

    @Test
    @DisplayName("markAsVerified() - Intent를 검증 완료 상태로 변경한다")
    void markAsVerified_ShouldChangeStatusToVerified() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );
        String transactionId = "txn_123456";

        // when
        intent.markAsVerified(transactionId);

        // then
        assertThat(intent.getStatus()).isEqualTo(IAPIntentStatus.VERIFIED);
        assertThat(intent.getTransactionId()).isEqualTo(transactionId);
        assertThat(intent.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsVerified() - 이미 검증된 Intent는 다시 검증할 수 없다")
    void markAsVerified_ShouldThrowException_WhenAlreadyVerified() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );
        intent.markAsVerified("txn_123456");

        // when & then
        assertThatThrownBy(() -> intent.markAsVerified("txn_789"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Intent already verified");
    }

    @Test
    @DisplayName("markAsFailed() - Intent를 실패 상태로 변경한다")
    void markAsFailed_ShouldChangeStatusToFailed() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );
        String errorCode = "INVALID_RECEIPT";
        String errorMessage = "영수증 검증 실패";

        // when
        intent.markAsFailed(errorCode, errorMessage);

        // then
        assertThat(intent.getStatus()).isEqualTo(IAPIntentStatus.FAILED);
        assertThat(intent.getErrorCode()).isEqualTo(errorCode);
        assertThat(intent.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("isExpired() - 만료 시간이 지나지 않았으면 false를 반환한다")
    void isExpired_ShouldReturnFalse_WhenNotExpired() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );

        // when & then
        assertThat(intent.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isExpired() - 만료 시간이 지났으면 true를 반환한다")
    void isExpired_ShouldReturnTrue_WhenExpired() {
        // given
        Instant pastTime = Instant.now().minusSeconds(7200); // 2시간 전
        IAPIntent intent = new IAPIntent(
            IAPIntentId.generate(),
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points"),
            IAPIntentStatus.PENDING,
            null,
            null,
            null,
            pastTime,
            pastTime.plusSeconds(3600), // 1시간 전 만료
            null
        );

        // when & then
        assertThat(intent.isExpired()).isTrue();
    }

    @Test
    @DisplayName("markAsExpired() - PENDING 상태이고 만료된 Intent를 EXPIRED 상태로 변경한다")
    void markAsExpired_ShouldChangeStatusToExpired_WhenPendingAndExpired() {
        // given
        Instant pastTime = Instant.now().minusSeconds(7200);
        IAPIntent intent = new IAPIntent(
            IAPIntentId.generate(),
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points"),
            IAPIntentStatus.PENDING,
            null,
            null,
            null,
            pastTime,
            pastTime.plusSeconds(3600),
            null
        );

        // when
        intent.markAsExpired();

        // then
        assertThat(intent.getStatus()).isEqualTo(IAPIntentStatus.EXPIRED);
    }

    @Test
    @DisplayName("markAsExpired() - VERIFIED 상태의 Intent는 만료 처리하지 않는다")
    void markAsExpired_ShouldNotChangeStatus_WhenVerified() {
        // given
        Instant pastTime = Instant.now().minusSeconds(7200);
        IAPIntent intent = new IAPIntent(
            IAPIntentId.generate(),
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points"),
            IAPIntentStatus.VERIFIED,
            "txn_123",
            null,
            null,
            pastTime,
            pastTime.plusSeconds(3600),
            Instant.now()
        );

        // when
        intent.markAsExpired();

        // then
        assertThat(intent.getStatus()).isEqualTo(IAPIntentStatus.VERIFIED);
    }

    @Test
    @DisplayName("canVerify() - PENDING 상태이고 만료되지 않았으면 true를 반환한다")
    void canVerify_ShouldReturnTrue_WhenPendingAndNotExpired() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );

        // when & then
        assertThat(intent.canVerify()).isTrue();
    }

    @Test
    @DisplayName("canVerify() - 만료된 Intent는 false를 반환한다")
    void canVerify_ShouldReturnFalse_WhenExpired() {
        // given
        Instant pastTime = Instant.now().minusSeconds(7200);
        IAPIntent intent = new IAPIntent(
            IAPIntentId.generate(),
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points"),
            IAPIntentStatus.PENDING,
            null,
            null,
            null,
            pastTime,
            pastTime.plusSeconds(3600),
            null
        );

        // when & then
        assertThat(intent.canVerify()).isFalse();
    }

    @Test
    @DisplayName("canVerify() - VERIFIED 상태의 Intent는 false를 반환한다")
    void canVerify_ShouldReturnFalse_WhenVerified() {
        // given
        IAPIntent intent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );
        intent.markAsVerified("txn_123");

        // when & then
        assertThat(intent.canVerify()).isFalse();
    }
}
