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

    @Query("SELECT e FROM IAPIntentEntity e " +
           "WHERE e.status = 'PENDING' AND e.expiresAt < :now")
    List<IAPIntentEntity> findExpiredIntents(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE IAPIntentEntity e SET e.status = 'EXPIRED' " +
           "WHERE e.status = 'PENDING' AND e.expiresAt < :now")
    int expireOldIntents(@Param("now") Instant now);
}
