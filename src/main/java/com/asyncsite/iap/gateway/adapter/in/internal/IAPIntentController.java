package com.asyncsite.iap.gateway.adapter.in.internal;

import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.iap.gateway.adapter.in.internal.dto.*;
import com.asyncsite.iap.gateway.application.port.in.CreateIAPIntentUseCase;
import com.asyncsite.iap.gateway.application.port.in.GetIAPIntentUseCase;
import com.asyncsite.iap.gateway.domain.intent.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Internal API (QueryDaily Mobile Service만 호출)
 *
 * 절대 외부 노출 안됨! (API Gateway에서 차단)
 */
@RestController
@RequestMapping("/internal/api/v1/iap/intents")
@RequiredArgsConstructor
@Slf4j
public class IAPIntentController {

    private final CreateIAPIntentUseCase createIntentUseCase;
    private final GetIAPIntentUseCase getIntentUseCase;

    /**
     * Intent 생성 (QueryDaily Mobile에서 호출)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateIntentResponse> createIntent(
        @Valid @RequestBody CreateIntentRequest request
    ) {
        log.info("POST /internal/api/v1/iap/intents: userEmail={}, productId={}",
                 request.getUserEmail(), request.getProductId());

        // Use Case 호출
        IAPIntent intent = createIntentUseCase.createIntent(
            UserEmail.of(request.getUserEmail()),
            ProductId.of(request.getProductId())
        );

        // Response 생성
        CreateIntentResponse response = CreateIntentResponse.builder()
            .intentId(intent.getIntentId().getValue())
            .userEmail(intent.getUserEmail().getValue())
            .productId(intent.getProductId().getValue())
            .status(intent.getStatus().name())
            .createdAt(intent.getCreatedAt().toString())
            .expiresAt(intent.getExpiresAt().toString())
            .build();

        return ApiResponse.success(response);
    }

    /**
     * Intent 조회 (폴링용)
     */
    @GetMapping("/{intentId}")
    public ApiResponse<GetIntentResponse> getIntent(
        @PathVariable String intentId
    ) {
        log.debug("GET /internal/api/v1/iap/intents/{}: intentId={}", intentId, intentId);

        // Use Case 호출
        IAPIntent intent = getIntentUseCase.getIntent(IAPIntentId.of(intentId));

        // Response 생성
        GetIntentResponse response = GetIntentResponse.builder()
            .intentId(intent.getIntentId().getValue())
            .userEmail(intent.getUserEmail().getValue())
            .productId(intent.getProductId().getValue())
            .status(intent.getStatus().name())
            .transactionId(intent.getTransactionId())
            .errorCode(intent.getErrorCode())
            .errorMessage(intent.getErrorMessage())
            .createdAt(intent.getCreatedAt().toString())
            .verifiedAt(intent.getVerifiedAt() != null ? intent.getVerifiedAt().toString() : null)
            .build();

        return ApiResponse.success(response);
    }
}
