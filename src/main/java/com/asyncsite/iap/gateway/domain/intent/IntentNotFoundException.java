package com.asyncsite.iap.gateway.domain.intent;

public class IntentNotFoundException extends RuntimeException {
    public IntentNotFoundException(String intentId) {
        super("Intent not found: " + intentId);
    }
}
