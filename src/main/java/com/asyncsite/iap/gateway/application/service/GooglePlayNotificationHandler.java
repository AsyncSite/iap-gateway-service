package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.adapter.in.pubsub.dto.GooglePlayNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Google Play 알림 처리 서비스
 *
 * Phase 4 Day 3에서 구현 예정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayNotificationHandler {

    /**
     * 구매 알림 처리
     *
     * @param notification Google Play RTDN
     */
    public void handlePurchaseNotification(GooglePlayNotification notification) {
        log.info("[GOOGLE PLAY] Processing purchase notification: productId={}, token={}...",
            notification.getProductId(), maskToken(notification.getPurchaseToken()));

        // TODO Phase 4 Day 3: Implement notification handling
        // 1. IAPIntent 조회 (productId와 PENDING 상태로 검색)
        // 2. Payment Core에 검증 요청
        // 3. Intent 상태 업데이트 (VERIFIED)
        // 4. Kafka 이벤트 발행

        log.warn("[GOOGLE PLAY] Notification handling not implemented yet - Phase 4 Day 3");
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
