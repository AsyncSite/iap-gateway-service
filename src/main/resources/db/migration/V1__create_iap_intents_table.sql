-- IAP Intent 테이블 (IAPIntent 도메인)
CREATE TABLE iap_intents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Intent 식별자 (appAccountToken으로 사용됨)
    intent_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'iap_intent_{timestamp}_{uuid}',

    -- 사용자 정보 (JWT에서 검증된 userId)
    user_email VARCHAR(100) NOT NULL COMMENT 'JWT 검증된 사용자 이메일',

    -- 상품 정보
    product_id VARCHAR(100) NOT NULL COMMENT 'insight_1000_points',

    -- 상태
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, VERIFIED, EXPIRED, FAILED',

    -- Transaction 참조 (완료 후)
    transaction_id VARCHAR(50) COMMENT 'Payment Core Transaction ID',

    -- 에러 정보
    error_code VARCHAR(50) COMMENT '실패 시 에러 코드',
    error_message TEXT COMMENT '실패 시 에러 메시지',

    -- TTL 관리
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL COMMENT '1시간 후 만료',
    verified_at TIMESTAMP(6) COMMENT '검증 완료 시각',
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    -- 인덱스
    INDEX idx_user_email (user_email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='IAP Intent 테이블 - userId 저장소';
