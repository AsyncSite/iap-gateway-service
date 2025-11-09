package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationData;
import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationPayload;
import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleTransactionInfo;
import com.asyncsite.iap.gateway.adapter.out.client.PaymentCoreClient;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationRequest;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationResponse;
import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentId;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentStatus;
import com.asyncsite.iap.gateway.domain.intent.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Apple Server Notification V2 처리 서비스
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreservernotifications
 *
 * <p>Flow:
 * <ol>
 *   <li>signedPayload JWT 검증 및 디코딩</li>
 *   <li>signedTransactionInfo JWT 디코딩</li>
 *   <li>appAccountToken에서 intentId 추출</li>
 *   <li>IAPIntent 조회</li>
 *   <li>Payment Core 검증 요청</li>
 *   <li>Intent 상태 업데이트 (VERIFIED)</li>
 *   <li>Kafka 이벤트 발행 (asyncsite.payment.verified)</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleNotificationHandler {

    private final AppleJwtVerifier jwtVerifier;
    private final PaymentCoreClient paymentCoreClient;
    private final IAPIntentRepository intentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Apple Server Notification 처리
     *
     * @param payload Apple Server Notification v2 Payload (signedPayload 포함)
     */
    @Transactional
    public void handleNotification(AppleNotificationPayload payload) {
        // 1. JWT 검증 및 디코딩 (signedPayload)
        AppleNotificationData notificationData = jwtVerifier.verifyAndDecode(
            payload.getSignedPayload(),
            AppleNotificationData.class
        );

        log.info("[APPLE] Notification type: {}, subtype: {}, environment: {}",
            notificationData.getNotificationType(),
            notificationData.getSubtype(),
            notificationData.getData().getEnvironment());

        // 2. CONSUMPTION_REQUEST 알림만 처리 (일회성 구매)
        if (!"CONSUMPTION_REQUEST".equals(notificationData.getNotificationType())) {
            log.info("[APPLE] Skipping non-consumption notification: type={}",
                notificationData.getNotificationType());
            return;
        }

        // 3. signedTransactionInfo 디코딩
        AppleTransactionInfo transaction = jwtVerifier.verifyAndDecode(
            notificationData.getData().getSignedTransactionInfo(),
            AppleTransactionInfo.class
        );

        log.info("[APPLE] Transaction: transactionId={}, productId={}, appAccountToken={}",
            transaction.getTransactionId(),
            transaction.getProductId(),
            transaction.getAppAccountToken());

        // 4. appAccountToken으로 Intent 조회
        String intentId = transaction.getAppAccountToken();
        if (intentId == null || intentId.isBlank()) {
            log.error("[APPLE] appAccountToken is missing in transaction");
            return;
        }

        IAPIntent intent = intentRepository.findById(IAPIntentId.of(intentId))
            .orElseThrow(() -> {
                log.error("[APPLE] Intent not found: {}", intentId);
                return new IllegalArgumentException("Intent not found: " + intentId);
            });

        log.info("[APPLE] Found intent: intentId={}, userEmail={}, status={}",
            intent.getIntentId().getValue(),
            intent.getUserEmail().getValue(),
            intent.getStatus());

        // 4-1. 중복 알림 검증 (이미 VERIFIED 상태인지 확인)
        if (intent.getStatus() == IAPIntentStatus.VERIFIED) {
            log.info("[APPLE] Intent already verified: intentId={}, status=VERIFIED, skipping duplicate notification",
                intent.getIntentId().getValue());
            return;
        }

        // 5. Payment Core에 검증 요청
        IAPVerificationRequest verificationRequest = IAPVerificationRequest.builder()
            .intentId(intent.getIntentId().getValue())
            .userEmail(intent.getUserEmail().getValue())
            .productId(transaction.getProductId())
            .platform(Platform.IOS)
            .purchaseToken(null)  // Apple은 purchaseToken 개념이 없음
            .build();

        try {
            IAPVerificationResponse verificationResponse = paymentCoreClient.verifyReceipt(verificationRequest);

            if (!verificationResponse.isSuccess()) {
                log.error("[APPLE] Verification failed: intentId={}, error={}",
                    intent.getIntentId().getValue(), verificationResponse.getErrorMessage());
                intent.markAsFailed(verificationResponse.getErrorMessage());
                intentRepository.save(intent);
                return;
            }

            log.info("[APPLE] Verification succeeded: intentId={}, platformTxId={}",
                intent.getIntentId().getValue(), verificationResponse.getPlatformTransactionId());

            // 6. Intent 상태 업데이트 (VERIFIED)
            intent.markAsVerified(transaction.getTransactionId());
            intentRepository.save(intent);

            // 7. Kafka 이벤트 발행
            publishPaymentVerifiedEvent(intent, verificationResponse);

            log.info("[APPLE] Purchase notification processed successfully: intentId={}, transactionId={}",
                intent.getIntentId().getValue(), transaction.getTransactionId());

        } catch (Exception e) {
            log.error("[APPLE] Failed to verify purchase: intentId={}, error={}",
                intent.getIntentId().getValue(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Kafka 이벤트 발행
     *
     * @param intent IAPIntent
     * @param verification 검증 응답
     */
    private void publishPaymentVerifiedEvent(IAPIntent intent, IAPVerificationResponse verification) {
        Map<String, Object> event = Map.of(
            "intentId", intent.getIntentId().getValue(),
            "userEmail", intent.getUserEmail().getValue(),
            "productId", intent.getProductId().getValue(),
            "platform", Platform.IOS.name(),  // Use enum for consistency
            "transactionId", verification.getPlatformTransactionId(),
            "insightAmount", verification.getInsightAmount(),
            "verifiedAt", verification.getVerifiedAt()
        );

        kafkaTemplate.send("asyncsite.payment.verified", event);
        log.info("[APPLE] Published payment.verified event: intentId={}", intent.getIntentId().getValue());
    }
}
