package com.asyncsite.iap.gateway.adapter.out.client;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Payment Core Feign Client 설정
 */
@Configuration
public class PaymentCoreFeignConfig {

    /**
     * Retry 설정
     *
     * - 초기 대기: 500ms
     * - 최대 대기: 2초
     * - 최대 재시도: 3회
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            500,    // period (초기 재시도 간격)
            2000,   // maxPeriod (최대 재시도 간격)
            3       // maxAttempts (최대 재시도 횟수)
        );
    }

    /**
     * Error Decoder
     *
     * HTTP 상태 코드별 예외 매핑
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new PaymentCoreErrorDecoder();
    }
}
