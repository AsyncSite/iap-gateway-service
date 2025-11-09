package com.asyncsite.iap.gateway.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.interfaces.ECPublicKey;
import java.util.List;
import java.util.Map;

/**
 * Apple JWT 검증기
 *
 * <p>Apple App Store Server Notifications V2의 JWT를 검증하고 디코딩합니다.
 *
 * <p>참고:
 * <ul>
 *   <li>https://developer.apple.com/documentation/appstoreservernotifications/responsebodyv2</li>
 *   <li>https://developer.apple.com/documentation/appstoreserverapi/jwstransactiondecodedpayload</li>
 * </ul>
 *
 * <p>Flow:
 * <ol>
 *   <li>JWT에서 kid (Key ID) 추출</li>
 *   <li>Apple 공개 키 다운로드 (https://appleid.apple.com/auth/keys)</li>
 *   <li>kid에 맞는 공개 키 선택</li>
 *   <li>ECDSA 서명 검증</li>
 *   <li>Payload 디코딩</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleJwtVerifier {

    private static final String APPLE_PUBLIC_KEY_URL = "https://appleid.apple.com/auth/keys";

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * JWT 검증 및 디코딩
     *
     * @param jwtString JWT 문자열 (예: eyJhbGciOiJFUzI1NiIsIng1YyI6W...)
     * @param clazz 디코딩할 클래스 타입
     * @return 디코딩된 Payload 객체
     * @throws IllegalArgumentException JWT 검증 실패 시
     */
    public <T> T verifyAndDecode(String jwtString, Class<T> clazz) {
        try {
            log.debug("[APPLE JWT] Parsing JWT...");
            SignedJWT signedJWT = SignedJWT.parse(jwtString);

            // 1. Apple 공개 키 다운로드
            log.debug("[APPLE JWT] Downloading Apple public key...");
            ECPublicKey publicKey = downloadApplePublicKey(signedJWT);

            // 2. 서명 검증
            log.debug("[APPLE JWT] Verifying signature...");
            JWSVerifier verifier = new ECDSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("JWT signature verification failed");
            }

            log.info("[APPLE JWT] Signature verified successfully");

            // 3. Payload 디코딩
            String payload = signedJWT.getPayload().toString();
            log.debug("[APPLE JWT] Payload: {}", payload);

            return objectMapper.readValue(payload, clazz);

        } catch (Exception e) {
            log.error("[APPLE JWT] JWT verification failed: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Apple JWT: " + e.getMessage(), e);
        }
    }

    /**
     * Apple 공개 키 다운로드
     *
     * <p>Apple의 JWK Set에서 kid에 맞는 공개 키를 다운로드합니다.
     *
     * @param signedJWT SignedJWT 객체
     * @return ECPublicKey
     * @throws Exception 공개 키 다운로드 또는 파싱 실패 시
     */
    private ECPublicKey downloadApplePublicKey(SignedJWT signedJWT) throws Exception {
        // kid (Key ID) 추출
        String kid = signedJWT.getHeader().getKeyID();
        if (kid == null) {
            throw new IllegalArgumentException("JWT header does not contain 'kid'");
        }

        log.debug("[APPLE JWT] Key ID (kid): {}", kid);

        // Apple 공개 키 다운로드
        WebClient webClient = webClientBuilder.build();
        Map<String, Object> response = webClient.get()
            .uri(APPLE_PUBLIC_KEY_URL)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (response == null || !response.containsKey("keys")) {
            throw new IllegalStateException("Failed to download Apple public keys");
        }

        // keys 배열에서 kid에 맞는 키 찾기
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) response.get("keys");

        for (Map<String, Object> keyData : keys) {
            if (kid.equals(keyData.get("kid"))) {
                log.debug("[APPLE JWT] Found matching public key for kid: {}", kid);
                String keyJson = objectMapper.writeValueAsString(keyData);
                ECKey ecKey = ECKey.parse(keyJson);
                return ecKey.toECPublicKey();
            }
        }

        throw new IllegalArgumentException("Apple public key not found for kid: " + kid);
    }
}
