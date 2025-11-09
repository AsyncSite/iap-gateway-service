package com.asyncsite.iap.gateway.adapter.out.client;

/**
 * Payment Core 통신 예외
 */
public class PaymentCoreException extends RuntimeException {

    public PaymentCoreException(String message) {
        super(message);
    }

    public PaymentCoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
