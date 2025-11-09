package com.asyncsite.iap.gateway.adapter.in.webhook;

import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationPayload;
import com.asyncsite.iap.gateway.application.service.AppleNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Apple App Store Server Notifications V2 Webhook
 *
 * <p>Apple에서 결제 이벤트 발생 시 호출되는 Webhook 엔드포인트입니다.
 *
 * <p>참고: https://developer.apple.com/documentation/appstoreservernotifications/app-store-server-notifications-v2
 *
 * <p>⚠️ Public Endpoint - Apple에서 직접 호출 (인증 불필요)
 *
 * <p>Flow:
 * <ol>
 *   <li>Apple Server Notification v2 수신 (signedPayload)</li>
 *   <li>AppleNotificationHandler로 위임</li>
 *   <li>JWT 검증 및 디코딩</li>
 *   <li>Payment Core 검증 요청</li>
 *   <li>Intent 상태 업데이트 (VERIFIED)</li>
 *   <li>Kafka 이벤트 발행 (asyncsite.payment.verified)</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/iap/webhooks")
@RequiredArgsConstructor
public class AppleWebhookController {

    private final AppleNotificationHandler notificationHandler;

    /**
     * Apple Server Notification V2 수신
     *
     * <p>⚠️ Apple은 2xx 응답을 기대합니다. 에러 발생 시에도 200 OK를 반환하여 재시도를 방지합니다.
     *
     * @param notification Apple Server Notification v2 Payload (signedPayload 포함)
     */
    @PostMapping("/ios")
    @ResponseStatus(HttpStatus.OK)
    public void handleNotification(@RequestBody AppleNotificationPayload notification) {
        log.info("[APPLE] Received server notification (signedPayload length: {})",
            notification.getSignedPayload() != null ? notification.getSignedPayload().length() : 0);

        try {
            notificationHandler.handleNotification(notification);
            log.info("[APPLE] Notification processed successfully");

        } catch (Exception e) {
            log.error("[APPLE] Failed to process notification: error={}",
                e.getMessage(), e);

            // ⚠️ Apple은 2xx 응답을 기대함
            // 에러 발생 시에도 200 반환 (재시도 방지)
            // Internal 에러는 로그로만 기록
        }
    }
}
