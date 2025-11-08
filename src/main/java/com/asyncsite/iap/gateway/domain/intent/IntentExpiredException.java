package com.asyncsite.iap.gateway.domain.intent;

public class IntentExpiredException extends RuntimeException {
    public IntentExpiredException(String intentId) {
        super("Intent expired: " + intentId);
    }
}
