package com.asyncsite.iap.gateway.adapter.in.common;

import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.iap.gateway.domain.intent.IntentExpiredException;
import com.asyncsite.iap.gateway.domain.intent.IntentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 *
 * 모든 예외를 ApiResponse 형태로 통일하여 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IntentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleIntentNotFound(IntentNotFoundException ex) {
        log.warn("Intent not found: {}", ex.getMessage());
        return ApiResponse.error("INTENT_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(IntentExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ApiResponse<Void> handleIntentExpired(IntentExpiredException ex) {
        log.warn("Intent expired: {}", ex.getMessage());
        return ApiResponse.error("INTENT_EXPIRED", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);
        return ApiResponse.error("VALIDATION_FAILED", errorMessage, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ApiResponse.error("INVALID_ARGUMENT", ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ApiResponse.error("ILLEGAL_STATE", ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ApiResponse.error("INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", null);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
