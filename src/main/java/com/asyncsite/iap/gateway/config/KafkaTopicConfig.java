package com.asyncsite.iap.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic 자동 생성 설정
 *
 * Producer가 토픽을 생성하는 주체입니다.
 * - 로컬/Docker: auto.create.topics.enable=true로 자동 생성되지만, 명시적 정의 권장
 * - 서버: 명시적으로 토픽을 정의하여 파티션/복제본 설정 제어
 */
@Configuration
public class KafkaTopicConfig {

    // IAP 관련 토픽명 상수
    public static final String IAP_VERIFICATION_STARTED = "asyncsite.iap.verification.started";
    public static final String INSIGHT_CHARGED = "asyncsite.insight.charged";
    public static final String INSIGHT_CHARGE_FAILED = "asyncsite.insight.charge.failed";

    /**
     * IAP 검증 시작 이벤트 토픽
     * - Producer: IAP Gateway Service
     * - Consumer: Payment Core Service
     */
    @Bean
    public NewTopic iapVerificationStartedTopic() {
        return TopicBuilder.name(IAP_VERIFICATION_STARTED)
                .partitions(3)          // 파티션 수 (처리량에 따라 조정)
                .replicas(1)            // 복제본 수 (로컬: 1, 프로덕션: 3 권장)
                .compact()              // 압축 활성화 (선택)
                .build();
    }

    /**
     * 인사이트 충전 성공 이벤트 토픽
     * - Producer: QueryDaily Mobile Service
     * - Consumer: IAP Gateway Service (필요시)
     */
    @Bean
    public NewTopic insightChargedTopic() {
        return TopicBuilder.name(INSIGHT_CHARGED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 인사이트 충전 실패 이벤트 토픽 (보상 트랜잭션)
     * - Producer: QueryDaily Mobile Service
     * - Consumer: IAP Gateway Service
     */
    @Bean
    public NewTopic insightChargeFailedTopic() {
        return TopicBuilder.name(INSIGHT_CHARGE_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
