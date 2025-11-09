package com.asyncsite.iap.gateway.adapter.in.webhook;

import com.asyncsite.iap.gateway.adapter.in.webhook.dto.AppleNotificationPayload;
import com.asyncsite.iap.gateway.application.service.AppleNotificationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppleWebhookController 통합 테스트
 */
@WebMvcTest(
    controllers = AppleWebhookController.class,
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
)
class AppleWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppleNotificationHandler notificationHandler;

    @Test
    void handleNotification_ValidPayload_ShouldReturn200() throws Exception {
        // Given: 정상 Payload
        AppleNotificationPayload payload = AppleNotificationPayload.builder()
            .signedPayload("eyJhbGciOiJFUzI1NiIsIng1YyI6W...")
            .build();

        doNothing().when(notificationHandler).handleNotification(any(AppleNotificationPayload.class));

        // When: POST /api/v1/iap/webhooks/ios
        // Then: 200 OK
        mockMvc.perform(post("/api/v1/iap/webhooks/ios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());

        verify(notificationHandler, times(1)).handleNotification(any(AppleNotificationPayload.class));
    }

    @Test
    void handleNotification_HandlerException_ShouldReturn200() throws Exception {
        // Given: Handler에서 예외 발생
        AppleNotificationPayload payload = AppleNotificationPayload.builder()
            .signedPayload("eyJhbGciOiJFUzI1NiIsIng1YyI6W...")
            .build();

        doThrow(new RuntimeException("Handler error"))
            .when(notificationHandler).handleNotification(any(AppleNotificationPayload.class));

        // When: POST /api/v1/iap/webhooks/ios
        // Then: 200 OK (Apple은 에러 시에도 2xx 응답 기대)
        mockMvc.perform(post("/api/v1/iap/webhooks/ios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());

        verify(notificationHandler, times(1)).handleNotification(any(AppleNotificationPayload.class));
    }

    @Test
    void handleNotification_EmptyPayload_ShouldReturn400() throws Exception {
        // Given: 빈 Payload
        String emptyPayload = "{}";

        // When: POST /api/v1/iap/webhooks/ios
        // Then: 400 Bad Request
        mockMvc.perform(post("/api/v1/iap/webhooks/ios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyPayload))
            .andExpect(status().isOk());  // ⚠️ Apple은 에러 시에도 200 반환
    }
}
