package com.asyncsite.iap.gateway.adapter.in.internal;

import com.asyncsite.iap.gateway.adapter.in.common.GlobalExceptionHandler;
import com.asyncsite.iap.gateway.application.port.in.CreateIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.in.GetIAPIntentUseCase;
import com.asyncsite.iap.gateway.domain.intent.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IAPIntentController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
    })
@Import(GlobalExceptionHandler.class)
@DisplayName("IAPIntentController 통합 테스트")
class IAPIntentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateIAPIntentUseCase createIAPIntentUseCase;

    @MockitoBean
    private GetIAPIntentUseCase getIAPIntentUseCase;

    @Test
    @DisplayName("POST /internal/api/v1/iap/intents - Intent를 성공적으로 생성한다")
    void createIntent_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("userEmail", "test@example.com");
        request.put("productId", "insight_1000_points");

        IAPIntent mockIntent = IAPIntent.create(
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points")
        );
        given(createIAPIntentUseCase.createIntent(any(UserEmail.class), any(ProductId.class)))
            .willReturn(mockIntent);

        // when & then
        mockMvc.perform(post("/internal/api/v1/iap/intents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.intentId").exists())
            .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
            .andExpect(jsonPath("$.data.productId").value("insight_1000_points"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.expiresAt").exists());

        then(createIAPIntentUseCase).should(times(1))
            .createIntent(any(UserEmail.class), any(ProductId.class));
    }

    @Test
    @DisplayName("POST /internal/api/v1/iap/intents - 유효하지 않은 이메일로 요청 시 400 반환")
    void createIntent_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("userEmail", "invalid-email");
        request.put("productId", "insight_1000_points");

        // when & then
        mockMvc.perform(post("/internal/api/v1/iap/intents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        then(createIAPIntentUseCase).should(never())
            .createIntent(any(UserEmail.class), any(ProductId.class));
    }

    @Test
    @DisplayName("POST /internal/api/v1/iap/intents - 필수 필드 누락 시 400 반환")
    void createIntent_ShouldReturnBadRequest_WhenMissingFields() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("userEmail", "test@example.com");
        // productId 누락

        // when & then
        mockMvc.perform(post("/internal/api/v1/iap/intents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        then(createIAPIntentUseCase).should(never())
            .createIntent(any(UserEmail.class), any(ProductId.class));
    }

    @Test
    @DisplayName("GET /internal/api/v1/iap/intents/{intentId} - Intent를 성공적으로 조회한다")
    void getIntent_ShouldReturnOk_WhenIntentExists() throws Exception {
        // given
        String intentId = "iap_intent_1234567890_abc123";
        IAPIntent mockIntent = new IAPIntent(
            IAPIntentId.of(intentId),
            UserEmail.of("test@example.com"),
            ProductId.of("insight_1000_points"),
            IAPIntentStatus.VERIFIED,
            "txn_123456",
            null,
            null,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Instant.now()
        );
        given(getIAPIntentUseCase.getIntent(any(IAPIntentId.class)))
            .willReturn(mockIntent);

        // when & then
        mockMvc.perform(get("/internal/api/v1/iap/intents/{intentId}", intentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.intentId").value(intentId))
            .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
            .andExpect(jsonPath("$.data.productId").value("insight_1000_points"))
            .andExpect(jsonPath("$.data.status").value("VERIFIED"))
            .andExpect(jsonPath("$.data.transactionId").value("txn_123456"));

        then(getIAPIntentUseCase).should(times(1))
            .getIntent(any(IAPIntentId.class));
    }

    @Test
    @DisplayName("GET /internal/api/v1/iap/intents/{intentId} - 존재하지 않는 Intent 조회 시 404 반환")
    void getIntent_ShouldReturnNotFound_WhenIntentDoesNotExist() throws Exception {
        // given
        String intentId = "iap_intent_1234567890_abc123";
        given(getIAPIntentUseCase.getIntent(any(IAPIntentId.class)))
            .willThrow(new IntentNotFoundException(intentId));

        // when & then
        mockMvc.perform(get("/internal/api/v1/iap/intents/{intentId}", intentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INTENT_NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").value("Intent not found: " + intentId));

        then(getIAPIntentUseCase).should(times(1))
            .getIntent(any(IAPIntentId.class));
    }

    @Test
    @DisplayName("GET /internal/api/v1/iap/intents/{intentId} - 만료된 Intent 조회 시 410 반환")
    void getIntent_ShouldReturnGone_WhenIntentExpired() throws Exception {
        // given
        String intentId = "iap_intent_1234567890_abc123";
        given(getIAPIntentUseCase.getIntent(any(IAPIntentId.class)))
            .willThrow(new IntentExpiredException(intentId));

        // when & then
        mockMvc.perform(get("/internal/api/v1/iap/intents/{intentId}", intentId))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INTENT_EXPIRED"))
            .andExpect(jsonPath("$.error.message").value("Intent expired: " + intentId));

        then(getIAPIntentUseCase).should(times(1))
            .getIntent(any(IAPIntentId.class));
    }
}
