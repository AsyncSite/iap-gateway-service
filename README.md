# IAP Gateway Service

IAP (In-App Purchase) Gateway Service는 QueryDaily Mobile 앱의 인앱 결제를 오케스트레이션하는 서비스입니다.

## 서비스 개요

- **포트**: 6084
- **역할**: 인앱 결제 오케스트레이터 (Checkout Service의 IAP 버전)
- **책임**:
  - IAPIntent 관리 (JWT 검증된 userId 저장소)
  - Server Notification 수신 (Google Play RTDN, Apple Server Notifications v2)
  - Payment Core 호출 (영수증 저장 및 Transaction 생성)
  - Kafka 이벤트 발행 (asyncsite.payment.verified)

## 아키텍처

```
QueryDaily Mobile Service (Public)
        ↓ Feign Client
IAP Gateway Service (Internal, 하지만 Webhook은 Public)
        ↓ Feign Client
Payment Core Service (Internal Only)
```

## 기술 스택

- Java 21
- Spring Boot 3.5.3
- Spring Cloud 2025.0.0
- MySQL 8.0
- Redis
- Kafka
- Flyway

## 로컬 실행

### 사전 요구사항

1. MySQL 데이터베이스 생성:
```sql
CREATE DATABASE iap_gateway_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Redis 실행:
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

3. Kafka 실행:
```bash
docker run -d -p 9092:9092 apache/kafka:latest
```

### 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API 엔드포인트

### Internal API (QueryDaily Mobile Service 전용)

- `POST /internal/api/v1/iap/intents` - IAPIntent 생성
- `GET /internal/api/v1/iap/intents/{id}` - IAPIntent 조회

### Public API (Webhook)

- `POST /api/v1/iap/webhooks/ios` - Apple Server Notification v2 (Phase 5)
- Google Play RTDN은 Pub/Sub Pull Subscription으로 수신 (Phase 4)

## 데이터베이스 스키마

- `iap_intents`: IAPIntent 엔티티 (userId 매핑, 상태 관리)

## 환경 변수

- `SPRING_PROFILES_ACTIVE`: 활성 프로파일 (local, docker)
- `SPRING_DATASOURCE_URL`: MySQL URL
- `SPRING_DATASOURCE_USERNAME`: MySQL 사용자명
- `SPRING_DATASOURCE_PASSWORD`: MySQL 비밀번호
- `SPRING_DATA_REDIS_HOST`: Redis 호스트
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka 서버

## Phase 1 구현 사항

- [x] Spring Boot 프로젝트 생성
- [x] 기본 설정 (application.yml)
- [ ] IAPIntent 도메인 모델
- [ ] Persistence 레이어
- [ ] Internal API
- [ ] Redis 캐싱

## 관련 문서

- [IAP Implementation Plan](../checkout-service/docs/plan/IAP_IMPLEMENTATION_PLAN.md)
- [Phase 1: IAP Gateway](../checkout-service/docs/plan/IAP_PHASE_1_IAP_GATEWAY.md)
