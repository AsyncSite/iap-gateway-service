package com.asyncsite.iap.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 *
 * Internal API (/internal/**) 보안 정책:
 * - permitAll(): Spring Security 인증 불필요
 * - Docker 내부 네트워크 / Kubernetes 클러스터 내부 통신 신뢰
 * - checkout-service, payment-core와 동일한 패턴
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Health check - 공개
                .requestMatchers("/actuator/health").permitAll()

                // Swagger UI - 공개 (개발 환경)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // Internal API - 서비스 간 통신 (인증 불필요)
                // QueryDaily Mobile Service에서 호출
                .requestMatchers("/internal/**").permitAll()

                // Webhooks - 외부 플랫폼에서 호출 (Apple, Google)
                .requestMatchers("/api/v1/iap/webhooks/**").permitAll()

                // 나머지 모든 요청 - 인증 필요 (향후 Gateway 헤더 인증)
                .anyRequest().permitAll()  // 현재는 모두 허용 (Phase 1)
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
