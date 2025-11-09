package com.asyncsite.iap.gateway.adapter.in.pubsub;

import com.asyncsite.iap.gateway.adapter.in.pubsub.dto.GooglePlayNotification;
import com.asyncsite.iap.gateway.application.service.GooglePlayNotificationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Google Play Pub/Sub Listener
 *
 * ⭐ RTDN 메시지를 수신하여 IAP 검증 처리
 *
 * @ConditionalOnProperty로 로컬 개발 환경에서는 비활성화
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "google.pubsub.enabled", havingValue = "true")
public class GooglePlayPubSubListener implements MessageReceiver {

    private final GooglePlayNotificationHandler notificationHandler;
    private final ObjectMapper objectMapper;

    @Value("${google.pubsub.project-id}")
    private String projectId;

    @Value("${google.pubsub.subscription-id}")
    private String subscriptionId;

    private Subscriber subscriber;

    /**
     * Pub/Sub Subscriber 시작
     */
    @PostConstruct
    public void startSubscriber() {
        log.info("[GOOGLE PLAY] Starting Pub/Sub subscriber: project={}, subscription={}",
            projectId, subscriptionId);

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        subscriber = Subscriber.newBuilder(subscriptionName, this).build();
        subscriber.startAsync().awaitRunning();

        log.info("[GOOGLE PLAY] Pub/Sub subscriber started successfully");
    }

    /**
     * Pub/Sub Subscriber 정지
     */
    @PreDestroy
    public void stopSubscriber() {
        if (subscriber != null) {
            log.info("[GOOGLE PLAY] Stopping Pub/Sub subscriber...");
            subscriber.stopAsync().awaitTerminated();
            log.info("[GOOGLE PLAY] Pub/Sub subscriber stopped");
        }
    }

    /**
     * Pub/Sub 메시지 수신 (MessageReceiver 구현)
     */
    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            log.info("[GOOGLE PLAY] Received Pub/Sub message: messageId={}", message.getMessageId());

            // 1. Base64 디코딩
            byte[] decodedData = Base64.getDecoder().decode(message.getData().toByteArray());
            String jsonData = new String(decodedData);

            log.debug("[GOOGLE PLAY] Message data: {}", jsonData);

            // 2. JSON 파싱
            GooglePlayNotification notification = objectMapper.readValue(
                jsonData,
                GooglePlayNotification.class
            );

            // 3. 테스트 알림 무시
            if (notification.isTestNotification()) {
                log.info("[GOOGLE PLAY] Test notification received, acknowledging");
                consumer.ack();
                return;
            }

            // 4. 일회성 구매 알림이 아닌 경우 스킵
            if (!notification.isOneTimeProductNotification()) {
                log.info("[GOOGLE PLAY] Skipping notification type: {}", notification.getNotificationType());
                consumer.ack();
                return;
            }

            // 5. 구매 알림만 처리 (notificationType=1)
            if (!notification.isPurchased()) {
                log.info("[GOOGLE PLAY] Skipping non-purchase notification: notificationType={}",
                    notification.getOneTimeProductNotification().getNotificationType());
                consumer.ack();
                return;
            }

            // 6. 알림 처리
            notificationHandler.handlePurchaseNotification(notification);

            // 7. Ack (처리 완료)
            consumer.ack();
            log.info("[GOOGLE PLAY] Message processed and acknowledged: messageId={}",
                message.getMessageId());

        } catch (Exception e) {
            log.error("[GOOGLE PLAY] Failed to process message: messageId={}, error={}",
                message.getMessageId(), e.getMessage(), e);

            // ⚠️ Nack → 재시도 (최대 3회)
            consumer.nack();
        }
    }
}
