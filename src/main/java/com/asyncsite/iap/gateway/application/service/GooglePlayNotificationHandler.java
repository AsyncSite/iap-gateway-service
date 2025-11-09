package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.adapter.in.pubsub.dto.GooglePlayNotification;
import com.asyncsite.iap.gateway.adapter.out.client.PaymentCoreClient;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationRequest;
import com.asyncsite.iap.gateway.adapter.out.client.dto.IAPVerificationResponse;
import com.asyncsite.iap.gateway.application.port.out.IAPIntentRepository;
import com.asyncsite.iap.gateway.domain.intent.IAPIntent;
import com.asyncsite.iap.gateway.domain.intent.IAPIntentStatus;
import com.asyncsite.iap.gateway.domain.intent.Platform;
import com.asyncsite.iap.gateway.domain.intent.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Google Play 알림 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayNotificationHandler {

    private final PaymentCoreClient paymentCoreClient;
    private final IAPIntentRepository intentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 구매 알림 처리
     *
     * @param notification Google Play RTDN
     */
    @Transactional
    public void handlePurchaseNotification(GooglePlayNotification notification) {
        String productId = notification.getProductId();
        String purchaseToken = notification.getPurchaseToken();

        log.info("[GOOGLE PLAY] Processing purchase notification: productId={}, token={}...",
            productId, maskToken(purchaseToken));

        // 1. IAPIntent 조회 (productId와 PENDING 상태로 검색)
        IAPIntent intent = findPendingIntent(productId);
        if (intent == null) {
            log.warn("[GOOGLE PLAY] No pending intent found for productId={}", productId);
            return;
        }

        // 1-1. 중복 알림 검증 (이미 VERIFIED 상태인지 확인)
        if (intent.getStatus() == IAPIntentStatus.VERIFIED) {
            log.info("[GOOGLE PLAY] Intent already verified: intentId={}, status=VERIFIED, skipping duplicate notification",
                intent.getIntentId().getValue());
            return;
        }

        log.info("[GOOGLE PLAY] Found pending intent: intentId={}, userEmail={}",
            intent.getIntentId().getValue(), intent.getUserEmail().getValue());

        // 2. Payment Core에 검증 요청
        IAPVerificationRequest verificationRequest = IAPVerificationRequest.builder()
            .intentId(intent.getIntentId().getValue())
            .userEmail(intent.getUserEmail().getValue())
            .productId(productId)
            .platform(Platform.ANDROID)
            .purchaseToken(purchaseToken)
            .build();

        try {
            IAPVerificationResponse verificationResponse = paymentCoreClient.verifyReceipt(verificationRequest);

            if (!verificationResponse.isSuccess()) {
                log.error("[GOOGLE PLAY] Verification failed: intentId={}, error={}",
                    intent.getIntentId().getValue(), verificationResponse.getErrorMessage());
                intent.markAsFailed(verificationResponse.getErrorMessage());
                intentRepository.save(intent);
                return;
            }

            log.info("[GOOGLE PLAY] Verification succeeded: intentId={}, platformTxId={}",
                intent.getIntentId().getValue(), verificationResponse.getPlatformTransactionId());

            // 3. Intent 상태 업데이트 (VERIFIED)
            intent.markAsVerified(verificationResponse.getPlatformTransactionId());
            intentRepository.save(intent);

            // 4. Kafka 이벤트 발행
            publishPaymentVerifiedEvent(intent, verificationResponse);

            log.info("[GOOGLE PLAY] Purchase notification processed successfully: intentId={}",
                intent.getIntentId().getValue());

        } catch (Exception e) {
            log.error("[GOOGLE PLAY] Failed to verify purchase: intentId={}, error={}",
                intent.getIntentId().getValue(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PENDING 상태의 Intent 조회
     *
     * ⚠️ CRITICAL TODO (Phase 4.5 또는 Phase 6):
     * 현재 구현은 productId로만 검색하여 다른 사용자의 구매와 매칭될 위험이 있음!
     *
     * Google Play RTDN의 한계:
     * - RTDN 메시지에는 appAccountToken이 포함되지 않음
     * - purchaseToken만 제공됨
     *
     * 해결 방안 (다음 중 하나 선택):
     * 1. Payment Core에서 Google Play API 호출 시 obfuscatedExternalAccountId 추출
     *    → IAPVerificationResponse에 appAccountToken 포함
     *    → appAccountToken으로 Intent 재조회
     *
     * 2. Intent 생성 시 purchaseToken 예측값을 DB에 저장
     *    → RTDN에서 purchaseToken으로 Intent 검색
     *
     * 3. Intent 테이블에 productId + createdAt 복합 인덱스 추가
     *    → 최근 30초 이내 생성된 Intent만 검색 (타임윈도우 매칭)
     *
     * 현재는 단일 사용자 테스트 환경이므로 productId 검색으로 충분하나,
     * 프로덕션 환경에서는 반드시 수정 필요!
     */
    private IAPIntent findPendingIntent(String productId) {
        return intentRepository.findTopByProductIdAndStatusOrderByCreatedAtDesc(
            ProductId.of(productId),
            IAPIntentStatus.PENDING
        ).orElse(null);
    }

    /**
     * Kafka 이벤트 발행
     */
    private void publishPaymentVerifiedEvent(IAPIntent intent, IAPVerificationResponse verification) {
        Map<String, Object> event = Map.of(
            "intentId", intent.getIntentId().getValue(),
            "userEmail", intent.getUserEmail().getValue(),
            "productId", intent.getProductId().getValue(),
            "platform", Platform.ANDROID.name(),  // Use enum for consistency
            "transactionId", verification.getPlatformTransactionId(),
            "insightAmount", verification.getInsightAmount(),
            "verifiedAt", verification.getVerifiedAt()
        );

        kafkaTemplate.send("asyncsite.payment.verified", event);
        log.info("[GOOGLE PLAY] Published payment.verified event: intentId={}", intent.getIntentId().getValue());
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
