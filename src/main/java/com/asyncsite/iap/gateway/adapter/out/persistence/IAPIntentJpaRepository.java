package com.asyncsite.iap.gateway.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IAPIntentJpaRepository extends JpaRepository<IAPIntentEntity, Long> {

    Optional<IAPIntentEntity> findByIntentId(String intentId);

    List<IAPIntentEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    /**
     * ProductId와 Status로 가장 최근 Intent 조회 (Phase 4 Google Play 알림 처리용)
     *
     * @param productId 상품 ID
     * @param status Intent 상태
     * @return 가장 최근 Intent
     */
    Optional<IAPIntentEntity> findTopByProductIdAndStatusOrderByCreatedAtDesc(String productId, String status);

    @Query("SELECT e FROM IAPIntentEntity e " +
           "WHERE e.status = 'PENDING' AND e.expiresAt < :now")
    List<IAPIntentEntity> findExpiredIntents(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE IAPIntentEntity e SET e.status = 'EXPIRED' " +
           "WHERE e.status = 'PENDING' AND e.expiresAt < :now")
    int expireOldIntents(@Param("now") Instant now);
}
