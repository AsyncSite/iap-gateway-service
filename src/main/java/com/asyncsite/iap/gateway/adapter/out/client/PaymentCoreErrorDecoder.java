package com.asyncsite.iap.gateway.adapter.out.client;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment Core Feign Client Error Decoder
 */
@Slf4j
public class PaymentCoreErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[Feign] Payment Core error: status={}, method={}",
            response.status(), methodKey);

        return switch (response.status()) {
            case 400 -> new PaymentCoreException("Invalid IAP verification request");
            case 404 -> new PaymentCoreException("Payment Core endpoint not found");
            case 409 -> new PaymentCoreException("Duplicate receipt (already verified)");

            // 5xx 에러는 재시도 가능
            case 502, 503, 504 -> new RetryableException(
                response.status(),
                "Payment Core unavailable",
                response.request().httpMethod(),
                (Long) null,
                response.request()
            );

            default -> new PaymentCoreException(
                "Payment Core error: " + response.status()
            );
        };
    }
}
