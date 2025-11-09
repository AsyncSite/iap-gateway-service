package com.asyncsite.iap.gateway.application.service;

import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AppleJwtVerifier 단위 테스트
 *
 * ⚠️ 실제 Apple JWT 검증은 통합 테스트에서 수행
 */
@ExtendWith(MockitoExtension.class)
class AppleJwtVerifierTest {

    private AppleJwtVerifier appleJwtVerifier;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        WebClient.Builder webClientBuilder = WebClient.builder();
        appleJwtVerifier = new AppleJwtVerifier(webClientBuilder, objectMapper);
    }

    @Test
    void verifyAndDecode_InvalidJwtFormat_ShouldThrowException() {
        // Given: 잘못된 JWT 형식
        String invalidJwt = "invalid-jwt-string";

        // When & Then: IllegalArgumentException 발생
        assertThatThrownBy(() ->
            appleJwtVerifier.verifyAndDecode(invalidJwt, AppleNotificationData.class)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid Apple JWT");
    }

    @Test
    void verifyAndDecode_NullJwt_ShouldThrowException() {
        // Given: null JWT
        String nullJwt = null;

        // When & Then: IllegalArgumentException 발생
        assertThatThrownBy(() ->
            appleJwtVerifier.verifyAndDecode(nullJwt, AppleNotificationData.class)
        )
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyAndDecode_EmptyJwt_ShouldThrowException() {
        // Given: 빈 JWT
        String emptyJwt = "";

        // When & Then: IllegalArgumentException 발생
        assertThatThrownBy(() ->
            appleJwtVerifier.verifyAndDecode(emptyJwt, AppleNotificationData.class)
        )
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_ShouldCreateInstance() {
        // Given & When: AppleJwtVerifier 생성
        // Then: 정상 생성 확인
        assertNotNull(appleJwtVerifier);
    }
}
