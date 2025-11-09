package com.asyncsite.iap.gateway.adapter.in.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Apple App Store Server Notifications Webhook
 *
 * Apple에서 결제 이벤트 발생 시 호출되는 Webhook 엔드포인트입니다.
 *
 * 참고: https://developer.apple.com/documentation/appstoreservernotifications
 *
 * Phase 4-5에서 구현 예정:
 * - Server Notification v2 처리
 * - JWT 검증
 * - Intent 조회 및 검증
 * - Payment Core 호출
 * - Kafka 이벤트 발행
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/iap/webhooks/ios")
@RequiredArgsConstructor
public class AppleWebhookController {

    /**
     * Apple Server Notification 수신
     *
     * @param payload Apple Server Notification v2 Payload (JWT)
     * @return 200 OK (Apple requires 200 response)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleNotification(@RequestBody String payload) {
        log.info("[Apple Webhook] Received notification (payload length: {})", payload.length());
        log.debug("[Apple Webhook] Payload: {}", payload);

        // TODO Phase 5: Implement Apple Server Notification v2 processing
        // 1. JWT 검증
        // 2. appAccountToken에서 intentId 추출
        // 3. IAPIntent 조회
        // 4. Payment Core 검증 요청
        // 5. Intent 상태 업데이트 (VERIFIED)
        // 6. Kafka 이벤트 발행 (asyncsite.payment.verified)

        log.warn("[Apple Webhook] Not implemented yet - Phase 5");
    }
}
