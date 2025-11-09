# IAP Gateway Service - AI Assistant Guidelines

## 🚨🚨🚨 절대 필수 규칙 🚨🚨🚨

### ⚠️ 빌드 및 파일 수정 시 반드시 준수할 규칙

**1. 빌드 시 개별 서비스 통과 100% 무조건 시키고 통과 안하면 다음 단계 진행하지 말 것**
- 모든 테스트가 100% 통과해야만 다음 단계로 진행
- 테스트 실패 시 반드시 문제를 해결한 후 진행
- 테스트 스킵이나 우회 절대 금지

**2. 파일 수정할 때는 일부분만 보고 하지 말고 해당 파일 전체를 무조건 읽고 전부 이해한 다음에 수정할 것**
- 파일의 일부만 읽고 수정하는 것 절대 금지
- 반드시 전체 파일을 읽고 전체 맥락을 이해한 후 수정
- 의존성과 연관성을 파악한 후 작업 진행

## 🚨 필독 - 로컬 빌드 전 반드시 확인!

### ⚠️ Docker 빌드 표준 준수 필수
**이 서비스를 빌드하기 전에 반드시 [Docker 빌드 표준 문서](../core-platform/docs/development/DOCKER_BUILD_STANDARDS.md)를 읽고 따르세요.**

### 🔨 IAP Gateway Service 빌드 방법
```bash
# IAP Gateway Service 빌드 (테스트 포함 - 필수)
cd ~/IdeaProjects/iap-gateway-service
./gradlew dockerRebuildAndRunIAPOnly

# 빠른 재빌드 (개발용만 - 테스트 스킵)
./gradlew dockerQuickRebuildIAPOnly

# 로그 확인
./gradlew dockerLogsIAPOnly

# 컨테이너 중지
./gradlew dockerDownIAPOnly

# 인프라만 실행 (로컬 개발)
./gradlew runInfraOnly
```

**절대 금지사항:**
- ❌ `./gradlew build -x test` (프로덕션 빌드시 테스트 스킵 금지)
- ❌ `docker build/run` 수동 실행 금지
- ❌ 테스트 실패 무시하고 진행 금지

테스트가 실패하면 **반드시 테스트를 통과시킨 후** 빌드하세요.

---

### 🔌 로컬 MySQL 접속 방법
```bash
# Docker MySQL 컨테이너 접속
docker exec -it asyncsite-mysql mysql -uroot -pasyncsite_root_2024!

# IAP Gateway DB 선택
USE iap_gateway_db;

# 데이터 확인 예시
SELECT * FROM iap_intents LIMIT 10;
SELECT * FROM iap_transactions LIMIT 10;
```

**데이터베이스 정보:**
- Host: `localhost` (로컬) / `asyncsite-mysql` (Docker)
- Port: `3306`
- Database: `iap_gateway_db`
- Username: `root`
- Password: `asyncsite_root_2024!`

---

## 🌍 환경 프로필 (Environment Profiles)

### 프로필 구조

IAP Gateway Service는 3가지 환경 프로필을 지원합니다:

```
src/main/resources/
├── application.yml              # 공통 설정 + 환경변수 기본값 (localhost)
├── application-local.yml        # 로컬 개발 환경 (DEBUG 로깅)
├── application-docker.yml       # Docker Compose 환경
└── application-prod.yml         # 프로덕션 환경 (INFO 로깅, 최적화)
```

### 프로필별 특징

| 프로필 | 용도 | 호스트 | 로깅 레벨 | SQL 로깅 | 추적 |
|-------|------|-------|---------|---------|------|
| **local** | 로컬 개발 | localhost | DEBUG | O | 전체 활성화 |
| **docker** | Docker Compose 로컬 테스트 | asyncsite-* | DEBUG | O | 전체 활성화 |
| **prod** | 프로덕션 배포 | 환경변수 | INFO | X | HTTP만 |

### 1️⃣ 로컬 개발 환경 (local)

**기본 프로필 - 별도 설정 불필요**

```bash
# 방법 1: 기본값 사용 (권장)
./gradlew bootRun

# 방법 2: 명시적 지정
./gradlew bootRun --args='--spring.profiles.active=local'

# 방법 3: 환경변수
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

**설정 값:**
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- Eureka: `localhost:8761`
- 로깅: DEBUG 레벨

### 2️⃣ Docker Compose 환경 (docker)

**Docker Compose로 로컬에서 전체 인프라 테스트**

```bash
# docker-compose.yml에 환경변수 설정
environment:
  SPRING_PROFILES_ACTIVE: docker

# 또는 Gradle task 사용
./gradlew dockerRebuildAndRunIAPOnly
```

**설정 값:**
- MySQL: `asyncsite-mysql:3306`
- Redis: `asyncsite-redis:6379`
- Kafka: `asyncsite-kafka:9092`
- Eureka: `asyncsite-eureka:8761`
- 로깅: DEBUG 레벨 (로컬 테스트용)

### 3️⃣ 프로덕션 환경 (prod)

**GCP/AWS 등 실제 운영 환경**

```bash
# 환경변수로 모든 설정 주입
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_HOST=production-mysql.example.com
export MYSQL_PORT=3306
export MYSQL_DATABASE=iap_gateway_db
export MYSQL_USERNAME=iap_user
export MYSQL_PASSWORD=secure_password

export SPRING_DATA_REDIS_HOST=production-redis.example.com
export SPRING_DATA_REDIS_PORT=6379

export KAFKA_BOOTSTRAP_SERVERS=production-kafka:9092
export EUREKA_URL=http://production-eureka:8761/eureka/
export PAYMENT_CORE_URL=http://production-payment-core:6082

./gradlew bootRun
```

**최적화 설정:**
- SQL 로깅 비활성화 (성능)
- Kafka 추적 비활성화 (성능)
- 요청/응답 로깅 비활성화 (보안)
- INFO 레벨 로깅
- Health endpoint 상세 정보는 인증된 요청만 노출

### 환경변수 전체 목록

| 환경변수 | 기본값 | 설명 |
|---------|-------|------|
| `SPRING_PROFILES_ACTIVE` | `local` | 활성 프로필 |
| `MYSQL_HOST` | `localhost` | MySQL 호스트 |
| `MYSQL_PORT` | `3306` | MySQL 포트 |
| `MYSQL_DATABASE` | `iap_gateway_db` | 데이터베이스명 |
| `MYSQL_USERNAME` | `root` | MySQL 사용자 |
| `MYSQL_PASSWORD` | `asyncsite_root_2024!` | MySQL 비밀번호 |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis 호스트 |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis 포트 |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka 서버 |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka 서버 |
| `PAYMENT_CORE_URL` | `http://localhost:6082` | Payment Core 서비스 URL |
| `JPA_HIBERNATE_DDL_AUTO` | `validate` | Hibernate DDL 모드 |
| `JPA_SHOW_SQL` | `true` | SQL 로깅 여부 |
| `FLYWAY_ENABLED` | `true` | Flyway 활성화 |
| `LOG_LEVEL_IAP_GATEWAY` | `DEBUG` | IAP Gateway 로그 레벨 |

### 프로필 전환 예시

```bash
# 로컬 개발 → Docker 테스트
export SPRING_PROFILES_ACTIVE=docker
./gradlew bootRun

# Docker → 프로덕션
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_HOST=production-mysql
# ... 기타 환경변수 설정
./gradlew bootRun

# 프로덕션 → 로컬
unset SPRING_PROFILES_ACTIVE  # 기본값 local 사용
./gradlew bootRun
```

### ⚠️ 주의사항

1. **프로덕션 환경에서는 반드시 환경변수로 설정 주입**
   - 설정 파일에 실제 비밀번호 하드코딩 금지
   - Kubernetes Secret, AWS Parameter Store 등 활용

2. **local/docker 프로필은 개발/테스트 전용**
   - 실제 운영 환경에서 사용 금지

3. **프로필 파일 우선순위**
   - `application-{profile}.yml` > `application.yml`
   - 동일한 속성은 프로필 파일이 우선

---

## 리팩토링 가이드

### 1️⃣ 단위 테스트
```bash
cd ~/IdeaProjects/iap-gateway-service
./gradlew test

# 실패 시 HTML 리포트 확인
open build/reports/tests/test/index.html
```

### 2️⃣ Docker 빌드 테스트
```bash
cd ~/IdeaProjects/iap-gateway-service

# 5분 타임아웃 필수
./gradlew dockerRebuildAndRunIAPOnly -Dorg.gradle.daemon.idletimeout=300000

# 빌드 확인
docker logs asyncsite-iap-gateway-service --tail 50
```

### 3️⃣ 테스트 결과 확인 위치

- **단위 테스트**: `/build/reports/tests/test/index.html`
- **Docker 로그**: `docker logs asyncsite-iap-gateway-service`

### ⚠️ 필수 확인사항
- 단위 테스트 전부 통과
- Docker 빌드 성공 (타임아웃 5분)
- 서비스 정상 실행 확인

---

## 1. 프로젝트 개요

IAP Gateway Service는 Google Cloud IAP (Identity-Aware Proxy)와 통합되어 In-App Purchase 처리를 담당하는 게이트웨이 서비스입니다.

### 핵심 기능
- IAP 영수증 검증 (Google Play/App Store)
- IAP Intent 생성 및 관리
- 구매 이벤트 발행 (Kafka)
- 영수증 캐싱 (Redis)

### 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.3, Spring Cloud Gateway
- **Build Tool**: Gradle 8.12
- **Database**: MySQL 8.0 (Flyway 마이그레이션)
- **Cache**: Redis 7
- **Message Broker**: Apache Kafka (토픽 자동 생성)
- **Service Discovery**: Netflix Eureka (예정)
- **Container**: Docker

### Kafka 토픽 자동 생성

**Producer가 토픽 생성의 주체입니다.**

IAP Gateway Service는 애플리케이션 시작 시 자동으로 Kafka 토픽을 생성합니다.

#### 자동 생성되는 토픽

| 토픽명 | 파티션 | 복제본 | Producer | Consumer | 용도 |
|--------|--------|--------|----------|----------|------|
| `asyncsite.iap.verification.started` | 3 | 1 | IAP Gateway | Payment Core | IAP 검증 시작 이벤트 |
| `asyncsite.insight.charged` | 3 | 1 | QueryDaily Mobile | IAP Gateway | 인사이트 충전 성공 |
| `asyncsite.insight.charge.failed` | 3 | 1 | QueryDaily Mobile | IAP Gateway | 인사이트 충전 실패 (보상 트랜잭션) |

#### 토픽 생성 메커니즘

1. **KafkaTopicConfig**: 토픽 정의 (`@Bean NewTopic`)
2. **KafkaProducerConfig**: `KafkaAdmin` 빈이 애플리케이션 시작 시 자동 생성
3. **application.yml**: `spring.kafka.admin.auto-create: true`

#### 로컬/서버 모두 자동 생성 확인 완료 ✅

```bash
# 토픽 생성 확인
docker exec asyncsite-kafka kafka-topics --bootstrap-server localhost:9092 --list

# 토픽 상세 정보
docker exec asyncsite-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic asyncsite.iap.verification.started
```

**결과:**
```
Topic: asyncsite.iap.verification.started	PartitionCount: 3	ReplicationFactor: 1
Topic: asyncsite.insight.charged	PartitionCount: 3	ReplicationFactor: 1
Topic: asyncsite.insight.charge.failed	PartitionCount: 3	ReplicationFactor: 1
```

---

## 2. 헥사고날 아키텍처

### 패키지 구조 (필수 준수)
```
com.asyncsite.iap.gateway/
├── IAPGatewayApplication.java              # 메인 애플리케이션
├── config/                                  # 공통 설정
│   ├── FeignConfig.java
│   ├── KafkaProducerConfig.java
│   └── RedisConfig.java
│
├── iap/                                     # IAP 도메인
│   ├── domain/                              # 순수 비즈니스 로직
│   │   ├── model/
│   │   │   ├── IAPIntent.java
│   │   │   ├── IAPTransaction.java
│   │   │   └── Receipt.java
│   │   └── event/
│   │       └── IAPPurchasedEvent.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                          # UseCase 인터페이스
│   │   │   │   ├── VerifyReceiptUseCase.java
│   │   │   │   └── CreateIAPIntentUseCase.java
│   │   │   └── out/                         # Port 인터페이스
│   │   │       ├── LoadIAPIntentPort.java
│   │   │       ├── SaveIAPIntentPort.java
│   │   │       ├── GooglePlayPort.java
│   │   │       └── AppStorePort.java
│   │   └── service/                         # UseCase 구현
│   │       ├── IAPVerificationService.java
│   │       └── IAPIntentService.java
│   └── adapter/
│       ├── in/
│       │   └── web/
│       │       ├── IAPController.java
│       │       └── dto/                     # Request/Response DTO
│       └── out/
│           ├── persistence/                 # JPA
│           │   ├── IAPIntentRepositoryAdapter.java
│           │   └── JpaIAPIntentRepository.java
│           ├── client/                      # Google/Apple API 클라이언트
│           │   ├── GooglePlayAdapter.java
│           │   └── AppStoreAdapter.java
│           ├── cache/                       # Redis 캐시
│           │   └── ReceiptCacheAdapter.java
│           └── messaging/                   # Kafka 어댑터
│               └── IAPEventPublisher.java
└── common/                                  # 공통 컴포넌트
    └── filter/                              # GatewayAuthenticationFilter (예정)
```

### 핵심 아키텍처 원칙

**1. Port & Adapter 패턴 철저히 준수**
- Service는 Port 인터페이스에만 의존
- Repository 직접 의존 금지
- Adapter가 Port 구현

**2. 의존성 방향**
```
Adapter → Application → Domain
   ↓           ↓
 Spring     Pure Java
```

**3. Domain 계층**
- 프레임워크 의존성 ZERO
- 순수 비즈니스 로직만
- VO (Value Object) 활용

---

## 3. API 설계 가이드

### 공통 응답 형식 (ApiResponse)

**모든 REST API는 core-platform의 `ApiResponse<T>` 래퍼를 사용하여 일관된 응답 형식을 제공합니다.**

#### 의존성
```kotlin
// build.gradle.kts
implementation("com.asyncsite.coreplatform:common:1.1.0-SNAPSHOT")
```

#### Import
```java
import com.asyncsite.coreplatform.common.dto.ApiResponse;
import com.asyncsite.coreplatform.common.dto.ErrorDetail;
```

#### ApiResponse 구조 (Kotlin)
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
```

#### 사용 예시

**성공 응답:**
```java
// GET - 조회 (200 OK)
@GetMapping("/api/v1/iap/intents/{intentId}")
public ApiResponse<IAPIntentResponse> getIntent(@PathVariable String intentId) {
    IAPIntentResponse response = iapIntentService.getIntent(intentId);
    return ApiResponse.success(response);
}

// POST - 생성 (201 Created)
@PostMapping("/api/v1/iap/verify")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<VerifyReceiptResponse> verifyReceipt(@RequestBody VerifyReceiptRequest request) {
    VerifyReceiptResponse response = iapService.verifyReceipt(request);
    return ApiResponse.success(response);
}

// DELETE - 삭제 (204 No Content)
@DeleteMapping("/api/v1/iap/intents/{intentId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public ApiResponse<Void> deleteIntent(@PathVariable String intentId) {
    iapIntentService.deleteIntent(intentId);
    return ApiResponse.success(null);
}
```

**에러 응답:**
```java
// GlobalExceptionHandler에서 처리
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
    return ApiResponse.badRequest(e.getErrorCode(), e.getMessage());
}

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException e) {
    return ApiResponse.notFound("NOT_FOUND", e.getMessage());
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<?>> handleServerError(Exception e) {
    return ApiResponse.serverError("INTERNAL_ERROR", "서버 오류가 발생했습니다.");
}
```

#### 응답 JSON 예시

**성공:**
```json
{
  "success": true,
  "data": {
    "intentId": "intent_123",
    "platform": "google",
    "status": "VERIFIED"
  },
  "error": null,
  "timestamp": "2025-11-09T10:30:00"
}
```

**에러:**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_RECEIPT",
    "message": "영수증 검증에 실패했습니다.",
    "details": {
      "platform": "google",
      "errorCode": "INVALID_SIGNATURE"
    }
  },
  "timestamp": "2025-11-09T10:30:00"
}
```

#### 주요 메서드 정리

| 메서드 | 반환 타입 | HTTP 상태 | 용도 |
|--------|----------|----------|------|
| `success(data)` | `ApiResponse<T>` | - | 성공 응답 (본문만) |
| `ok(data)` | `ResponseEntity<ApiResponse<T>>` | 200 | 조회 성공 |
| `created(data)` | `ResponseEntity<ApiResponse<T>>` | 201 | 생성 성공 |
| `createdResponse(data)` | `ApiResponse<T>` | - | 생성 응답 (본문만) |
| `noContent()` | `ResponseEntity<ApiResponse<Void>>` | 204 | 성공 (응답 없음) |
| `badRequest(code, msg)` | `ResponseEntity<ApiResponse<T>>` | 400 | 잘못된 요청 |
| `notFound(code, msg)` | `ResponseEntity<ApiResponse<T>>` | 404 | 리소스 없음 |
| `serverError(code, msg)` | `ResponseEntity<ApiResponse<T>>` | 500 | 서버 오류 |
| `error(code, msg)` | `ApiResponse<T>` | - | 에러 응답 (본문만) |
| `errorWithStatus(...)` | `ResponseEntity<ApiResponse<T>>` | 커스텀 | 커스텀 에러 |

### ⭐ Controller 응답 규칙 (필수 준수)

**모든 Controller는 다음 규칙을 일관성 있게 따라야 합니다:**

#### 1. GET 요청 (조회)
```java
// ✅ 올바른 방식 - ApiResponse만 반환
@GetMapping("/api/v1/iap/intents/{intentId}")
public ApiResponse<IAPIntentResponse> getIntent(@PathVariable String intentId) {
    IAPIntentResponse response = service.getIntent(intentId);
    return ApiResponse.success(response);
}

// ❌ 잘못된 방식 - ResponseEntity 사용 금지
@GetMapping("/api/v1/iap/intents/{intentId}")
public ResponseEntity<ApiResponse<IAPIntentResponse>> getIntent(@PathVariable String intentId) {
    return ApiResponse.ok(response); // 사용하지 말 것
}
```

#### 2. POST 요청 (생성)
```java
// ✅ 올바른 방식 - @ResponseStatus + ApiResponse
@PostMapping("/api/v1/iap/verify")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<VerifyReceiptResponse> verifyReceipt(@RequestBody VerifyReceiptRequest request) {
    VerifyReceiptResponse response = service.verifyReceipt(request);
    return ApiResponse.success(response);
}

// ❌ 잘못된 방식 - ResponseEntity 사용 금지
@PostMapping("/api/v1/iap/verify")
public ResponseEntity<ApiResponse<VerifyReceiptResponse>> verifyReceipt(@RequestBody VerifyReceiptRequest request) {
    return ApiResponse.created(response); // 사용하지 말 것
}
```

#### 3. DELETE 요청 (삭제)
```java
// ✅ 올바른 방식 - @ResponseStatus + ApiResponse
@DeleteMapping("/api/v1/iap/intents/{intentId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public ApiResponse<Void> deleteIntent(@PathVariable String intentId) {
    service.deleteIntent(intentId);
    return ApiResponse.success(null);
}
```

#### 핵심 원칙
- **GET**: `ApiResponse<T>` + `ApiResponse.success()`
- **POST**: `@ResponseStatus(HttpStatus.CREATED)` + `ApiResponse<T>` + `ApiResponse.success()`
- **PUT/PATCH**: `ApiResponse<T>` + `ApiResponse.success()`
- **DELETE**: `@ResponseStatus(HttpStatus.NO_CONTENT)` + `ApiResponse<Void>` + `ApiResponse.success(null)`
- **ResponseEntity 사용 금지**: 모든 Controller에서 `ResponseEntity` 래퍼 사용 금지
- **HTTP 상태 코드**: `@ResponseStatus` 어노테이션으로 설정 (200 OK는 기본값이므로 생략)

### ⭐ GlobalExceptionHandler 응답 규칙 (필수 준수)

**모든 예외 핸들러는 `@ResponseStatus` + `ApiResponse.error()` 패턴을 사용해야 합니다:**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IntentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleIntentNotFound(IntentNotFoundException ex) {
        log.warn("Intent not found: {}", ex.getMessage());
        return ApiResponse.error("INTENT_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(IntentExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ApiResponse<Void> handleIntentExpired(IntentExpiredException ex) {
        log.warn("Intent expired: {}", ex.getMessage());
        return ApiResponse.error("INTENT_EXPIRED", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errorMessage);
        return ApiResponse.error("VALIDATION_FAILED", errorMessage, null);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
```

#### 핵심 원칙
- **절대 ResponseEntity로 감싸지 말 것**: ApiResponse가 이미 ResponseEntity를 내부에 포함
- **@ResponseStatus 어노테이션 필수**: HTTP 상태 코드 설정
- **ApiResponse.error() 사용**: 세 번째 파라미터 `details`는 null 또는 Map
- **일관된 에러 코드**: UPPER_SNAKE_CASE 형식 사용

### 엔드포인트 패턴
```
# IAP 영수증 검증
POST   /api/v1/iap/verify                 # 영수증 검증 (인증)

# IAP Intent
POST   /api/v1/iap/intents                # Intent 생성 (인증)
GET    /api/v1/iap/intents/{intentId}     # Intent 조회 (인증)
GET    /api/v1/me/iap/purchases           # 내 구매 목록 (인증)

# Health Check
GET    /actuator/health                   # 서비스 상태 (공개)
```

### 보안 설정
- **공개 API**: `/actuator/health`
- **보호 API**: 나머지 모든 API (Gateway JWT 인증 필수)
- **내부 API**: `/internal/**` (서비스 간 통신)

---

## 4. 데이터베이스 설계

### 주요 테이블

#### iap_intents (IAP Intent)
```sql
CREATE TABLE iap_intents (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    platform VARCHAR(20) NOT NULL,          # GOOGLE, APPLE
    product_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,            # PENDING, VERIFIED, FAILED
    receipt_data TEXT,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_platform_product (platform, product_id)
);
```

#### iap_transactions (구매 내역)
```sql
CREATE TABLE iap_transactions (
    id VARCHAR(36) PRIMARY KEY,
    intent_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,   # PG사 거래 ID
    amount DECIMAL(10,2),
    currency VARCHAR(3),
    purchased_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_id (transaction_id),
    FOREIGN KEY (intent_id) REFERENCES iap_intents(id)
);
```

---

## 5. 개발 규칙

### 코딩 컨벤션
- **클래스**: PascalCase (e.g., `IAPVerificationService`)
- **메서드**: camelCase (e.g., `verifyReceipt()`)
- **상수**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **패키지**: lowercase

### 필수 어노테이션
```java
@Service                    // Use Case 구현체
@Component                  // Adapter 구현체
@RestController            // REST Controller
@RequiredArgsConstructor   // Lombok 생성자 주입
@Slf4j                     // 로깅
@Transactional             // 트랜잭션 (UseCase 단위)
```

### 예외 처리
- 비즈니스 예외는 Result 패턴 사용 권장
- 기술적 예외는 Spring의 예외 처리 활용
- GlobalExceptionHandler에서 통합 처리
- 명확한 에러 메시지 제공

---

## 6. Port & Adapter 패턴 가이드

### 개요

**이 프로젝트는 순수 헥사고날 아키텍처(Port & Adapter 패턴)을 채택합니다.**

### Port 명명 규칙

| Port 타입 | 명명 규칙 | 역할 | 예시 |
|---------|---------|------|------|
| **Outbound - Load** | `LoadXxxPort` | 조회 전용 | `LoadIAPIntentPort` |
| **Outbound - Save** | `SaveXxxPort` | 저장/수정/삭제 | `SaveIAPIntentPort` |
| **Outbound - External** | `XxxPort` | 외부 API 호출 | `GooglePlayPort`, `AppStorePort` |

### Adapter 명명 규칙

| Adapter 타입 | 명명 규칙 | 역할 | 예시 |
|------------|---------|------|------|
| **Persistence** | `XxxRepositoryAdapter` | Port 구현 (JPA) | `IAPIntentRepositoryAdapter` |
| **Web** | `XxxController` | REST API | `IAPController` |
| **Client** | `XxxAdapter` | 외부 API 클라이언트 | `GooglePlayAdapter` |
| **Cache** | `XxxCacheAdapter` | 캐시 구현 | `ReceiptCacheAdapter` |
| **Messaging** | `XxxPublisher` | Kafka 메시지 발행 | `IAPEventPublisher` |

### 실제 구현 예시

#### 1. Port 인터페이스 정의

**조회 전용 Port (LoadIAPIntentPort.java):**
```java
package com.asyncsite.iap.gateway.iap.application.port.out;

import com.asyncsite.iap.gateway.iap.domain.model.IAPIntent;
import java.util.List;
import java.util.Optional;

public interface LoadIAPIntentPort {
    Optional<IAPIntent> findById(String intentId);
    List<IAPIntent> findByUserId(String userId);
    boolean existsByUserIdAndProductId(String userId, String productId);
}
```

**저장 전용 Port (SaveIAPIntentPort.java):**
```java
package com.asyncsite.iap.gateway.iap.application.port.out;

import com.asyncsite.iap.gateway.iap.domain.model.IAPIntent;

public interface SaveIAPIntentPort {
    IAPIntent save(IAPIntent iapIntent);
    void delete(IAPIntent iapIntent);
}
```

**외부 API Port (GooglePlayPort.java):**
```java
package com.asyncsite.iap.gateway.iap.application.port.out;

import com.asyncsite.iap.gateway.iap.domain.model.Receipt;

public interface GooglePlayPort {
    Receipt verifyReceipt(String receiptData, String productId);
}
```

#### 2. Service에서 Port 의존

**Application Service (IAPVerificationService.java):**
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class IAPVerificationService {

    // ✅ Port에 의존 (Infrastructure 직접 의존 X)
    private final LoadIAPIntentPort loadIAPIntentPort;
    private final SaveIAPIntentPort saveIAPIntentPort;
    private final GooglePlayPort googlePlayPort;
    private final AppStorePort appStorePort;

    @Transactional
    public IAPIntent verifyReceipt(String userId, String platform, String receiptData, String productId) {
        // 플랫폼별 검증
        Receipt receipt = switch (platform) {
            case "google" -> googlePlayPort.verifyReceipt(receiptData, productId);
            case "apple" -> appStorePort.verifyReceipt(receiptData, productId);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };

        // Intent 생성 및 저장
        IAPIntent intent = IAPIntent.builder()
                .userId(userId)
                .platform(platform)
                .productId(productId)
                .receiptData(receiptData)
                .status("VERIFIED")
                .verifiedAt(LocalDateTime.now())
                .build();

        return saveIAPIntentPort.save(intent);
    }
}
```

#### 3. Adapter에서 Port 구현

**Persistence Adapter (IAPIntentRepositoryAdapter.java):**
```java
@Component
@RequiredArgsConstructor
public class IAPIntentRepositoryAdapter implements LoadIAPIntentPort, SaveIAPIntentPort {

    private final JpaIAPIntentRepository jpaRepository;

    // ===== LoadIAPIntentPort 구현 =====

    @Override
    public Optional<IAPIntent> findById(String intentId) {
        return jpaRepository.findById(intentId);
    }

    @Override
    public List<IAPIntent> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, String productId) {
        return jpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    // ===== SaveIAPIntentPort 구현 =====

    @Override
    public IAPIntent save(IAPIntent iapIntent) {
        return jpaRepository.save(iapIntent);
    }

    @Override
    public void delete(IAPIntent iapIntent) {
        jpaRepository.delete(iapIntent);
    }
}
```

**External API Adapter (GooglePlayAdapter.java):**
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class GooglePlayAdapter implements GooglePlayPort {

    private final GooglePlayClient googlePlayClient;

    @Override
    public Receipt verifyReceipt(String receiptData, String productId) {
        try {
            GooglePlayResponse response = googlePlayClient.verifyPurchase(receiptData, productId);

            return Receipt.builder()
                    .platform("google")
                    .productId(productId)
                    .transactionId(response.getOrderId())
                    .isValid(response.getPurchaseState() == 0)
                    .purchasedAt(Instant.ofEpochMilli(response.getPurchaseTimeMillis()))
                    .build();
        } catch (Exception e) {
            log.error("Google Play verification failed", e);
            throw new IAPVerificationException("Google Play 영수증 검증 실패", e);
        }
    }
}
```

### 의존성 방향 원칙

```
┌─────────────────────────────────────────────┐
│          Application Layer                  │
│  ┌───────────────────────────────────────┐  │
│  │  Service (UseCase)                    │  │
│  └───────────────────────────────────────┘  │
│         ⬇️ 의존                              │
│  ┌───────────────────────────────────────┐  │
│  │  Port Interfaces (추상화)             │  │
│  │  - LoadXxxPort                        │  │
│  │  - SaveXxxPort                        │  │
│  │  - ExternalPort                       │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
                   ⬆️ 구현
┌─────────────────────────────────────────────┐
│           Adapter Layer                     │
│  ┌───────────────────────────────────────┐  │
│  │  XxxRepositoryAdapter (Port 구현)     │  │
│  │  XxxAdapter (External API)            │  │
│  └───────────────────────────────────────┘  │
│                   ⬇️ 의존                    │
│  ┌───────────────────────────────────────┐  │
│  │  JpaRepository / External Client      │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

---

## 7. 테스트 전략

### 테스트 구조
```
src/test/java/com/asyncsite/iap/gateway/
├── iap/
│   ├── domain/IAPIntentTest.java                  # 단위 테스트
│   └── application/IAPVerificationServiceTest.java # 통합 테스트
```

### 테스트 원칙
- 각 계층별 독립적인 테스트
- Mock 사용으로 의존성 분리
- H2 인메모리 DB로 통합 테스트
- @SpringBootTest 최소화
- Testcontainers로 MySQL 테스트 (필요시)

---

## 8. Docker 및 배포

### Docker 빌드 명령어
```bash
# 개발용 (권장)
./gradlew dockerRebuildAndRunIAPOnly

# 로그 확인
./gradlew dockerLogsIAPOnly

# 컨테이너 중지
./gradlew dockerDownIAPOnly

# 인프라만 실행 (로컬 개발)
./gradlew runInfraOnly
```

### 환경별 설정
- **local**: MySQL localhost, Kafka localhost, Eureka localhost
- **docker**: Docker 네트워크 (asyncsite-mysql, asyncsite-kafka, asyncsite-eureka)
- **prod**: GCP Cloud IAP 연동

---

## 9. 모니터링 및 로깅

### 헬스 체크
- Spring Actuator 사용
- `/actuator/health` 엔드포인트
- Docker HEALTHCHECK 설정

### 로깅 전략
- 각 요청마다 Correlation ID 추가 (common-tracer)
- 비즈니스 로직 실행 시점 로깅
- 에러 발생 시 상세 정보 포함
- 민감 정보 로깅 금지 (영수증 데이터, 개인정보)

---

## 10. 문제 해결 접근법 (Problem Solving Approach)

⚠️ **필수 준수 사항**: 모든 문제 해결 시 다음 5단계를 반드시 따라야 합니다.

1. **Think hard and deeply about the root cause**
   - 표면적 증상이 아닌 실제 문제의 근원을 파악하세요
   - "왜(Why)"를 최소 5번 반복하여 깊이 있게 분석하세요
   - 로그, 스택 트레이스, 검증 결과를 꼼꼼히 확인하세요

2. **Do a global inspection to understand the full context**
   - 변경이 영향을 미칠 모든 서비스와 컴포넌트를 검토하세요
   - Google/Apple IAP API, 내부 서비스들의 의존성을 확인하세요
   - 기존 IAP 아키텍처 문서를 참고하세요

3. **Find a stable, best-practice solution**
   - 검증된 IAP 패턴과 Spring Boot 베스트 프랙티스를 활용하세요
   - 영수증 검증 보안을 항상 고려하세요
   - 성능, 보안, 신뢰성을 최우선으로 하세요

4. **Ensure consistency with other services**
   - 다른 게이트웨이 서비스와의 일관성을 유지하세요
   - 공통 DTO, 이벤트 포맷을 준수하세요
   - core-platform의 common 모듈을 활용하세요

5. **Read CLAUDE.md if needed**
   - 불확실한 부분은 항상 이 가이드라인을 재확인하세요
   - IAP System Design 문서를 참고하세요
   - 다른 서비스의 CLAUDE.md도 확인하세요

---

## 11. 🚨 CRITICAL: AGENTS.md - Essential Development Rules

Problem definition → small, safe change → change review → refactor — repeat the loop.

### Mandatory Rules

- Before changing anything, read the relevant files end to end, including all call/reference paths.
- Keep tasks, commits, and PRs small.
- If you make assumptions, record them in the Issue/PR/ADR.
- Never commit or log secrets; validate all inputs and encode/normalize outputs.
- Avoid premature abstraction and use intention-revealing names.
- Compare at least two options before deciding.

### Mindset

- Think like a senior engineer.
- Don't jump in on guesses or rush to conclusions.
- Always evaluate multiple approaches; write one line each for pros/cons/risks, then choose the simplest solution.

### Code & File Reference Rules

- Read files thoroughly from start to finish (no partial reads).
- Before changing code, locate and read definitions, references, call sites, related tests, docs/config/flags.
- Do not change code without having read the entire file.
- Before modifying a symbol, run a global search to understand pre/postconditions and leave a 1–3 line impact note.

### Required Coding Rules

- Before coding, write a Problem 1-Pager: Context / Problem / Goal / Non-Goals / Constraints.
- Enforce limits: file ≤ 300 LOC, function ≤ 50 LOC, parameters ≤ 5, cyclomatic complexity ≤ 10. If exceeded, split/refactor.
- Prefer explicit code; no hidden "magic."
- Follow DRY, but avoid premature abstraction.
- Isolate side effects (I/O, network, global state) at the boundary layer.
- Catch only specific exceptions and present clear user-facing messages.
- Use structured logging and do not log sensitive data (propagate request/correlation IDs when possible).
- Account for time zones and DST.

### Testing Rules

- New code requires new tests; bug fixes must include a regression test (write it to fail first).
- Tests must be deterministic and independent; replace external systems with fakes/contract tests.
- Include ≥1 happy path and ≥1 failure path in e2e tests.
- Proactively assess risks from concurrency/locks/retries (duplication, deadlocks, etc.).

### Security Rules

- Never leave secrets in code/logs/tickets.
- Validate, normalize, and encode inputs; use parameterized operations.
- Apply the Principle of Least Privilege.

### Clean Code Rules

- Use intention-revealing names.
- Each function should do one thing.
- Keep side effects at the boundary.
- Prefer guard clauses first.
- Symbolize constants (no hardcoding).
- Structure code as Input → Process → Return.
- Report failures with specific errors/messages.
- Make tests serve as usage examples; include boundary and failure cases.

### Anti-Pattern Rules

- Don't modify code without reading the whole context.
- Don't expose secrets.
- Don't ignore failures or warnings.
- Don't introduce unjustified optimization or abstraction.
- Don't overuse broad exceptions.

---

## 12. AI 어시스턴트를 위한 중요 사항

1. **IAP는 실수가 허용되지 않는 영역**: 모든 변경사항을 신중히 검토
2. **영수증 검증은 필수**: 모든 IAP 요청은 서버 사이드 검증 필요
3. **강타입 선호**: Map<String, Object> 대신 명확한 DTO 사용
4. **이벤트 발행**: 모든 구매는 Kafka 이벤트로 발행
5. **보안 최우선**: 민감 정보 로깅 금지, 모든 입력 검증
6. **테스트 커버리지**: IAP 로직은 100% 테스트 커버리지 목표
7. **문서화**: IAP 검증 로직은 반드시 문서화
8. **버전 관리**: API 변경 시 하위 호환성 유지
9. **모니터링**: 모든 검증 실패는 즉시 알림
10. **전문적인 커밋**: 커밋 메시지에 AI/어시스턴트 언급 금지

### 필수 준수 사항
1. **헥사고날 아키텍처 엄격 적용**: 의존성 방향 절대 준수
2. **전체 파일 읽기**: 수정 전 반드시 전체 파일 이해
3. **테스트 우선**: 모든 변경사항에 테스트 추가
4. **커밋 메시지**: AI 언급 절대 금지, 전문적 메시지 작성

### 코드 수정 시 체크리스트
- [ ] Domain 계층의 외부 의존성 없음 확인
- [ ] Port 인터페이스의 순수성 유지
- [ ] Adapter 계층에서만 외부 기술 사용
- [ ] Service는 Port에만 의존 (Repository 직접 의존 금지)
- [ ] 테스트 커버리지 확인
- [ ] API 문서 업데이트

### 참고 서비스
- **querydaily-mobile-service**: 헥사고날 아키텍처 참조
- **payment-core**: 결제 엔진 참조
- **payment-gateway**: PG사 연동 참조

---

## 13. 현재 구현 상태 (2025.11.09)

### ✅ Phase 1 완료 (Day 1-5) 🎉
- ✅ 프로젝트 초기 설정 (Spring Boot 3.5.3, Java 21, Gradle 8.10.2)
- ✅ Gradle 빌드 스크립트 (core-platform:common 의존성 포함)
- ✅ Docker 설정 (docker-compose, Dockerfile, gradle tasks)
- ✅ Flyway 마이그레이션 스크립트 (V1__create_iap_intents_table.sql)
- ✅ **Domain Model 구현** (IAPIntent, Value Objects, Enums)
- ✅ **Persistence Layer 구현** (JPA, Mapper, Repository Port/Adapter)
- ✅ **Application Service 구현** (Use Cases, IAPIntentService)
- ✅ **Internal API 구현** (IAPIntentController, DTOs)
- ✅ **ApiResponse 통합** (core-platform:common)
- ✅ **GlobalExceptionHandler** (@ResponseStatus + ApiResponse.error 패턴)
- ✅ **단위 테스트** (46개 테스트, 100% 통과)
  - Domain Model 테스트 (IAPIntent, Value Objects)
  - Service 테스트 (IAPIntentService)
  - Controller 통합 테스트 (IAPIntentController)
- ✅ **Redis 캐싱 적용** (Day 5 완료)
  - RedisTemplate with JavaTimeModule (java.time.Instant 직렬화 지원)
  - createIntent: Intent 생성 시 자동 캐싱 (10분 TTL)
  - getIntent: Cache Hit/Miss 로직 구현
  - 만료된 캐시 자동 삭제
  - Redis 정상 작동 확인 (키 저장, TTL 확인 완료)

### 🚧 Phase 2-6 예정
- Phase 2: QueryDaily Mobile Service 연동
- Phase 3: Payment Core 연동
- Phase 4: Google Play Server Notification 수신
- Phase 5: Apple App Store Server Notification 수신
- Phase 6: 중복 방지 및 보안 강화

---

> **"Think hard and think deeply"** - 항상 근본 원인을 파악하고 전체적인 영향을 고려하여 개발하세요.