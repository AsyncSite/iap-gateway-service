package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationData;
import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationPayload;
import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleTransactionData;
import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleTransactionInfo;
import com.asyncsite.iap.gateway.adapter.out.client.PaymentCoreClient;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationRequest;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationResponse;
import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AppleNotificationHandler 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AppleNotificationHandlerTest {

    @Mock
    private AppleJwtVerifier jwtVerifier;

    @Mock
    private PaymentCoreClient paymentCoreClient;

    @Mock
    private IAPIntentRepository intentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AppleNotificationHandler handler;

    @Captor
    private ArgumentCaptor<Map<String, Object>> eventCaptor;

    private AppleNotificationPayload notificationPayload;
    private AppleNotificationData notificationData;
    private AppleTransactionInfo transactionInfo;
    private IAPIntent intent;

    @BeforeEach
    void setUp() {
        // AppleNotificationPayload 생성
        notificationPayload = AppleNotificationPayload.builder()
            .signedPayload("eyJhbGciOiJFUzI1NiIsIng1YyI6W...")  // Mock JWT
            .build();

        // AppleNotificationData 생성
        AppleTransactionData transactionData = AppleTransactionData.builder()
            .appAppleId(123456789L)
            .bundleId("com.asyncsite.querydaily")
            .bundleVersion("1.0.0")
            .environment("Sandbox")
            .signedTransactionInfo("eyJhbGciOiJFUzI1NiIsIng1YyI6W...")  // Mock JWT
            .build();

        notificationData = AppleNotificationData.builder()
            .notificationType("CONSUMPTION_REQUEST")
            .subtype("INITIAL_BUY")
            .notificationUUID("12345678-1234-1234-1234-123456789012")
            .version("2.0")
            .signedDate(Instant.now().toEpochMilli())
            .data(transactionData)
            .build();

        // AppleTransactionInfo 생성
        transactionInfo = AppleTransactionInfo.builder()
            .transactionId("2000000123456789")
            .originalTransactionId("2000000123456789")
            .productId("com.asyncsite.querydaily.insights.500")
            .purchaseDate(Instant.now().toEpochMilli())
            .originalPurchaseDate(Instant.now().toEpochMilli())
            .quantity(1)
            .type("Consumable")
            .appAccountToken("iap_intent_1699876543000_abc123")
            .inAppOwnershipType("PURCHASED")
            .signedDate(Instant.now().toEpochMilli())
            .environment("Sandbox")
            .build();

        // IAPIntent 생성
        intent = new IAPIntent(
            IAPIntentId.of("iap_intent_1699876543000_abc123"),
            UserEmail.of("user@example.com"),
            ProductId.of("com.asyncsite.querydaily.insights.500"),
            IAPIntentStatus.PENDING,
            null,  // transactionId
            null,  // errorCode
            null,  // errorMessage
            Instant.now(),
            Instant.now().plusSeconds(600),  // expiresAt (10분)
            null   // verifiedAt
        );
    }

    @Test
    void handleNotification_ValidNotification_ShouldVerifyIntent() {
        // Given: 정상 알림
        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);
        when(jwtVerifier.verifyAndDecode(eq(notificationData.getData().getSignedTransactionInfo()), eq(AppleTransactionInfo.class)))
            .thenReturn(transactionInfo);
        when(intentRepository.findById(any(IAPIntentId.class)))
            .thenReturn(Optional.of(intent));

        IAPVerificationResponse verificationResponse = IAPVerificationResponse.builder()
            .success(true)
            .platformTransactionId("2000000123456789")
            .insightAmount(500)
            .verifiedAt(Instant.now())
            .build();
        when(paymentCoreClient.verifyReceipt(any(IAPVerificationRequest.class)))
            .thenReturn(verificationResponse);

        // When: 알림 처리
        handler.handleNotification(notificationPayload);

        // Then: Intent VERIFIED, Kafka 이벤트 발행
        verify(intentRepository).save(any(IAPIntent.class));
        verify(kafkaTemplate).send(eq("asyncsite.payment.verified"), eventCaptor.capture());

        Map<String, Object> event = eventCaptor.getValue();
        assertThat(event).containsEntry("platform", "IOS");
        assertThat(event).containsEntry("insightAmount", 500);
        assertThat(event).containsKey("intentId");
    }

    @Test
    void handleNotification_NonConsumptionRequest_ShouldSkip() {
        // Given: CONSUMPTION_REQUEST가 아닌 알림
        notificationData.setNotificationType("REFUND");
        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);

        // When: 알림 처리
        handler.handleNotification(notificationPayload);

        // Then: 스킵, Payment Core 호출 안 함
        verify(paymentCoreClient, never()).verifyReceipt(any());
        verify(intentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void handleNotification_MissingAppAccountToken_ShouldSkip() {
        // Given: appAccountToken 없음
        transactionInfo.setAppAccountToken(null);
        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);
        when(jwtVerifier.verifyAndDecode(eq(notificationData.getData().getSignedTransactionInfo()), eq(AppleTransactionInfo.class)))
            .thenReturn(transactionInfo);

        // When: 알림 처리
        handler.handleNotification(notificationPayload);

        // Then: 스킵, Intent 조회 안 함
        verify(intentRepository, never()).findById(any());
        verify(paymentCoreClient, never()).verifyReceipt(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void handleNotification_IntentNotFound_ShouldThrowException() {
        // Given: Intent 없음
        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);
        when(jwtVerifier.verifyAndDecode(eq(notificationData.getData().getSignedTransactionInfo()), eq(AppleTransactionInfo.class)))
            .thenReturn(transactionInfo);
        when(intentRepository.findById(any(IAPIntentId.class)))
            .thenReturn(Optional.empty());

        // When & Then: IllegalArgumentException 발생
        assertThatThrownBy(() -> handler.handleNotification(notificationPayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Intent not found");
    }

    @Test
    void handleNotification_AlreadyVerified_ShouldSkip() {
        // Given: Intent가 이미 VERIFIED 상태
        IAPIntent verifiedIntent = new IAPIntent(
            IAPIntentId.of("iap_intent_1699876543000_abc123"),
            UserEmail.of("user@example.com"),
            ProductId.of("com.asyncsite.querydaily.insights.500"),
            IAPIntentStatus.VERIFIED,
            "2000000123456789",  // transactionId
            null,  // errorCode
            null,  // errorMessage
            Instant.now(),
            Instant.now().plusSeconds(600),  // expiresAt
            Instant.now()  // verifiedAt
        );

        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);
        when(jwtVerifier.verifyAndDecode(eq(notificationData.getData().getSignedTransactionInfo()), eq(AppleTransactionInfo.class)))
            .thenReturn(transactionInfo);
        when(intentRepository.findById(any(IAPIntentId.class)))
            .thenReturn(Optional.of(verifiedIntent));

        // When: 중복 알림 처리
        handler.handleNotification(notificationPayload);

        // Then: 스킵, Payment Core 호출 안 함, Kafka 이벤트 발행 안 함
        verify(paymentCoreClient, never()).verifyReceipt(any());
        verify(intentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void handleNotification_PaymentCoreFailure_ShouldMarkAsFailed() {
        // Given: Payment Core 검증 실패
        when(jwtVerifier.verifyAndDecode(eq(notificationPayload.getSignedPayload()), eq(AppleNotificationData.class)))
            .thenReturn(notificationData);
        when(jwtVerifier.verifyAndDecode(eq(notificationData.getData().getSignedTransactionInfo()), eq(AppleTransactionInfo.class)))
            .thenReturn(transactionInfo);
        when(intentRepository.findById(any(IAPIntentId.class)))
            .thenReturn(Optional.of(intent));

        IAPVerificationResponse failedResponse = IAPVerificationResponse.builder()
            .success(false)
            .errorMessage("Receipt validation failed")
            .build();
        when(paymentCoreClient.verifyReceipt(any(IAPVerificationRequest.class)))
            .thenReturn(failedResponse);

        // When: 알림 처리
        handler.handleNotification(notificationPayload);

        // Then: Intent FAILED, Kafka 이벤트 발행 안 함
        verify(intentRepository).save(any(IAPIntent.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
}
