package com.asyncsite.iap.gateway.adapter.out.persistence;

import com.asyncsite.iap.gateway.domain.intent.IAPIntentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "iap_intents")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IAPIntentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intent_id", unique = true, nullable = false, length = 50)
    private String intentId;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IAPIntentStatus status;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
