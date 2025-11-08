package com.asyncsite.iap.gateway.domain.intent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * IAP Intent 식별자 (appAccountToken으로 사용됨)
 *
 * 형식: iap_intent_{timestamp}_{uuid}
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IAPIntentId {

    private String value;

    public static IAPIntentId generate() {
        long timestamp = Instant.now().toEpochMilli();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return new IAPIntentId("iap_intent_" + timestamp + "_" + uuid);
    }

    public static IAPIntentId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IAPIntentId cannot be null or empty");
        }
        if (!value.startsWith("iap_intent_")) {
            throw new IllegalArgumentException("Invalid IAPIntentId format: " + value);
        }
        return new IAPIntentId(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IAPIntentId that = (IAPIntentId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
