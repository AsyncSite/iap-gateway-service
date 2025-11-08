package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IAPIntentService 테스트")
class IAPIntentServiceTest {

    @Mock
    private IAPIntentRepository intentRepository;

    @InjectMocks
    private IAPIntentService iapIntentService;

    private UserEmail userEmail;
    private ProductId productId;

    @BeforeEach
    void setUp() {
        userEmail = UserEmail.of("test@example.com");
        productId = ProductId.of("insight_1000_points");
    }

    @Test
    @DisplayName("createIntent() - 새로운 Intent를 생성하고 저장한다")
    void createIntent_ShouldCreateAndSaveIntent() {
        // given
        IAPIntent expectedIntent = IAPIntent.create(userEmail, productId);
        given(intentRepository.save(any(IAPIntent.class))).willReturn(expectedIntent);

        // when
        IAPIntent result = iapIntentService.createIntent(userEmail, productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserEmail()).isEqualTo(userEmail);
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getStatus()).isEqualTo(IAPIntentStatus.PENDING);
        then(intentRepository).should(times(1)).save(any(IAPIntent.class));
    }

    @Test
    @DisplayName("getIntent() - intentId로 Intent를 조회한다")
    void getIntent_ShouldReturnIntent_WhenExists() {
        // given
        IAPIntentId intentId = IAPIntentId.generate();
        IAPIntent expectedIntent = IAPIntent.create(userEmail, productId);
        given(intentRepository.findById(intentId)).willReturn(Optional.of(expectedIntent));

        // when
        IAPIntent result = iapIntentService.getIntent(intentId);

        // then
        assertThat(result).isEqualTo(expectedIntent);
        then(intentRepository).should(times(1)).findById(intentId);
    }

    @Test
    @DisplayName("getIntent() - 존재하지 않는 intentId로 조회 시 예외 발생")
    void getIntent_ShouldThrowException_WhenNotFound() {
        // given
        IAPIntentId intentId = IAPIntentId.of("iap_intent_1234567890_abc123");
        given(intentRepository.findById(intentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> iapIntentService.getIntent(intentId))
            .isInstanceOf(IntentNotFoundException.class)
            .hasMessageContaining("Intent not found");
        then(intentRepository).should(times(1)).findById(intentId);
    }

    @Test
    @DisplayName("getIntent() - 만료된 PENDING Intent 조회 시 예외 발생")
    void getIntent_ShouldThrowException_WhenExpired() {
        // given
        IAPIntentId intentId = IAPIntentId.generate();
        Instant pastTime = Instant.now().minusSeconds(7200);
        IAPIntent expiredIntent = new IAPIntent(
            intentId,
            userEmail,
            productId,
            IAPIntentStatus.PENDING,
            null,
            null,
            null,
            pastTime,
            pastTime.plusSeconds(3600), // 1시간 전 만료
            null
        );
        given(intentRepository.findById(intentId)).willReturn(Optional.of(expiredIntent));

        // when & then
        assertThatThrownBy(() -> iapIntentService.getIntent(intentId))
            .isInstanceOf(IntentExpiredException.class)
            .hasMessageContaining("Intent expired");
        then(intentRepository).should(times(1)).findById(intentId);
    }

    @Test
    @DisplayName("getIntent() - VERIFIED 상태의 Intent는 정상 조회된다")
    void getIntent_ShouldReturnIntent_WhenVerified() {
        // given
        IAPIntentId intentId = IAPIntentId.generate();
        IAPIntent verifiedIntent = IAPIntent.create(userEmail, productId);
        verifiedIntent.markAsVerified("txn_123456");
        given(intentRepository.findById(intentId)).willReturn(Optional.of(verifiedIntent));

        // when
        IAPIntent result = iapIntentService.getIntent(intentId);

        // then
        assertThat(result.getStatus()).isEqualTo(IAPIntentStatus.VERIFIED);
        assertThat(result.getTransactionId()).isEqualTo("txn_123456");
        then(intentRepository).should(times(1)).findById(intentId);
    }
}
