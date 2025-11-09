package com.asyncsite.iap.gateway.adapter.in.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Google Play Real-time Developer Notifications Webhook
 *
 * Google Play에서 결제 이벤트 발생 시 Pub/Sub을 통해 호출되는 Webhook 엔드포인트입니다.
 *
 * 참고: https://developer.android.com/google/play/billing/rtdn-reference
 *
 * Phase 4에서 구현 예정:
 * - Pub/Sub Message 처리
 * - DeveloperNotification 파싱
 * - Intent 조회 및 검증
 * - Payment Core 호출
 * - Kafka 이벤트 발행
 *
 * 주의: Google은 HTTP Push Endpoint가 아닌 Pub/Sub Pull Subscription을 권장합니다.
 *       실제 구현은 Pub/Sub Listener로 대체될 수 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/iap/webhooks/android")
@RequiredArgsConstructor
public class GoogleWebhookController {

    /**
     * Google Play RTDN 수신 (Pub/Sub Push 방식)
     *
     * @param payload Google Pub/Sub Push Message
     * @return 200 OK (Google requires 200 response)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleNotification(@RequestBody String payload) {
        log.info("[Google Webhook] Received notification (payload length: {})", payload.length());
        log.debug("[Google Webhook] Payload: {}", payload);

        // TODO Phase 4: Implement Google Play RTDN processing
        // 1. Pub/Sub Message 검증
        // 2. DeveloperNotification 파싱
        // 3. obfuscatedExternalAccountId에서 intentId 추출
        // 4. IAPIntent 조회
        // 5. Payment Core 검증 요청
        // 6. Intent 상태 업데이트 (VERIFIED)
        // 7. Kafka 이벤트 발행 (asyncsite.payment.verified)

        log.warn("[Google Webhook] Not implemented yet - Phase 4");
    }

    /**
     * 대안: Pub/Sub Pull Subscription Listener
     *
     * @implNote Phase 4에서 Pub/Sub Pull 방식으로 구현 시 이 Controller는 제거되고
     *           별도의 Pub/Sub Listener (@Component) 방식으로 대체됩니다.
     */
}
