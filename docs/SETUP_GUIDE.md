# IAP Gateway 설정 가이드 (완전 초보자용)

> 이 가이드는 Google Play와 Apple App Store IAP 설정을 처음 하시는 분들을 위한 단계별 가이드입니다.

---

## 📋 목차

1. [Google Play 설정](#1-google-play-설정)
2. [Apple App Store 설정](#2-apple-app-store-설정)
3. [환경 변수 설정](#3-환경-변수-설정)
4. [테스트 방법](#4-테스트-방법)

---

## 1. Google Play 설정

### 1-1. Google Cloud Console 설정

#### Step 1: Google Cloud 프로젝트 생성

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 상단 프로젝트 선택 드롭다운 클릭
3. "새 프로젝트" 클릭
4. 프로젝트 이름 입력 (예: `querydaily-iap-production`)
5. "만들기" 클릭
6. **프로젝트 ID를 메모장에 기록** (예: `querydaily-iap-xxxxx`)

#### Step 2: Pub/Sub API 활성화

1. 왼쪽 메뉴 → "API 및 서비스" → "라이브러리" 클릭
2. 검색창에 "Pub/Sub" 입력
3. "Cloud Pub/Sub API" 클릭
4. "사용 설정" 클릭

#### Step 3: Pub/Sub Topic 생성

1. 왼쪽 메뉴 → "Pub/Sub" → "주제" 클릭
2. "주제 만들기" 클릭
3. 주제 ID 입력: `asyncsite-iap-notifications`
4. "주제 만들기" 클릭
5. **전체 Topic 이름 기록** (예: `projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications`)

#### Step 4: Pub/Sub Subscription 생성

1. 방금 생성한 Topic 클릭
2. "구독 만들기" 클릭
3. 구독 ID 입력: `iap-gateway-rtdn-sub`
4. 전송 유형: **Pull** 선택 (중요!)
5. 메시지 보관 기간: **7일**
6. 승인 기한: **60초**
7. 재시도 정책: **즉시 재시도** 선택
8. "만들기" 클릭

#### Step 5: Service Account 생성

1. 왼쪽 메뉴 → "IAM 및 관리자" → "서비스 계정" 클릭
2. "서비스 계정 만들기" 클릭
3. 서비스 계정 이름: `iap-gateway-pubsub-reader`
4. "만들고 계속하기" 클릭
5. 역할 선택:
   - "Pub/Sub 구독자" 선택
   - (또는 검색창에 "Pub/Sub Subscriber" 입력)
6. "계속" → "완료" 클릭

#### Step 6: Service Account 키 다운로드

1. 방금 생성한 서비스 계정 클릭
2. "키" 탭 클릭
3. "키 추가" → "새 키 만들기" 클릭
4. 키 유형: **JSON** 선택 (중요!)
5. "만들기" 클릭
6. **JSON 파일이 자동 다운로드됨** (예: `querydaily-iap-xxxxx-abc123.json`)
7. 이 파일을 안전한 곳에 보관하고, 나중에 서버에 업로드할 예정

### 1-2. Google Play Console 설정

#### Step 1: Google Play Console 접속

1. [Google Play Console](https://play.google.com/console) 접속
2. QueryDaily 앱 선택 (없으면 먼저 앱 등록 필요)

#### Step 2: Real-time Developer Notifications (RTDN) 설정

1. 왼쪽 메뉴 → "수익 창출" → "수익 창출 설정" 클릭
2. "Cloud Pub/Sub 알림" 섹션 찾기
3. "주제 이름 입력" 필드에 다음 입력:
   ```
   projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications
   ```
   (위에서 기록한 전체 Topic 이름 사용)
4. "주제 이름 추가" 클릭
5. "변경사항 저장" 클릭

#### Step 3: 인앱 상품 등록

1. 왼쪽 메뉴 → "수익 창출" → "상품" → "인앱 상품" 클릭
2. "상품 만들기" 클릭

**상품 1: Insights 100개**
- 상품 ID: `com.asyncsite.querydaily.insights.100`
- 이름: `Insights 100개`
- 설명: `면접 답변 인사이트 100개`
- 가격: `$1.09` (또는 ₩1,500)
- "저장" → "활성화" 클릭

**상품 2: Insights 500개**
- 상품 ID: `com.asyncsite.querydaily.insights.500`
- 이름: `Insights 500개`
- 설명: `면접 답변 인사이트 500개`
- 가격: `$4.99` (또는 ₩6,500)
- "저장" → "활성화" 클릭

**상품 3: Insights 1000개**
- 상품 ID: `com.asyncsite.querydaily.insights.1000`
- 이름: `Insights 1000개`
- 설명: `면접 답변 인사이트 1000개`
- 가격: `$8.99` (또는 ₩11,900)
- "저장" → "활성화" 클릭

**상품 4: Insights 3000개**
- 상품 ID: `com.asyncsite.querydaily.insights.3000`
- 이름: `Insights 3000개`
- 설명: `면접 답변 인사이트 3000개`
- 가격: `$24.99` (또는 ₩32,900)
- "저장" → "활성화" 클릭

#### Step 4: 테스트 계정 추가 (Sandbox 테스트용)

1. 왼쪽 메뉴 → "설정" → "라이선스 테스트" 클릭
2. "라이선스 테스터 추가" 클릭
3. 테스트용 Gmail 계정 입력 (예: `yourtest@gmail.com`)
4. "저장" 클릭

---

## 2. Apple App Store 설정

### 2-1. App Store Connect 설정

#### Step 1: App Store Connect 접속

1. [App Store Connect](https://appstoreconnect.apple.com/) 접속
2. "나의 앱" 클릭
3. QueryDaily 앱 선택 (없으면 먼저 앱 등록 필요)

#### Step 2: Server Notifications v2 URL 설정

1. 왼쪽 메뉴 → "App 정보" → "App Store 서버 알림" 클릭
2. "Production Server URL" 입력:
   ```
   https://api.asyncsite.com/api/v1/iap/webhooks/ios
   ```
3. "Sandbox Server URL" 입력:
   ```
   https://api-dev.asyncsite.com/api/v1/iap/webhooks/ios
   ```
   (개발 서버가 없으면 Production URL과 동일하게 설정)
4. 알림 유형:
   - ✅ "SUBSCRIBED" (구독 시작)
   - ✅ "DID_RENEW" (갱신)
   - ✅ "REFUND" (환불)
   - ✅ "DID_FAIL_TO_RENEW" (갱신 실패)
5. "저장" 클릭

#### Step 3: 인앱 구매 상품 등록

1. 왼쪽 메뉴 → "기능" → "인앱 구매" 클릭
2. "+" 버튼 클릭

**상품 1: Insights 100개**
- 유형: **소모성 상품** 선택
- 참조 이름: `Insights 100개`
- 제품 ID: `com.asyncsite.querydaily.insights.100`
- 가격: **Tier 1** ($0.99) 또는 수동 입력 $1.09
- 저장 후 "검토 정보 추가" 클릭
  - 스크린샷 업로드 (필수)
  - 설명: `면접 답변 인사이트 100개`
- "제출" 클릭

**상품 2: Insights 500개**
- 유형: **소모성 상품**
- 참조 이름: `Insights 500개`
- 제품 ID: `com.asyncsite.querydaily.insights.500`
- 가격: **Tier 5** ($4.99)
- 검토 정보 추가 → 제출

**상품 3: Insights 1000개**
- 유형: **소모성 상품**
- 참조 이름: `Insights 1000개`
- 제품 ID: `com.asyncsite.querydaily.insights.1000`
- 가격: **Tier 10** ($9.99) 또는 수동 $8.99
- 검토 정보 추가 → 제출

**상품 4: Insights 3000개**
- 유형: **소모성 상품**
- 참조 이름: `Insights 3000개`
- 제품 ID: `com.asyncsite.querydaily.insights.3000`
- 가격: **Tier 25** ($24.99)
- 검토 정보 추가 → 제출

#### Step 4: Sandbox 테스트 계정 생성

1. App Store Connect 홈 → "사용자 및 액세스" 클릭
2. "Sandbox 테스터" 클릭
3. "+" 버튼 클릭
4. 테스트용 Apple ID 정보 입력:
   - 이메일: 새로운 이메일 (실제 Apple ID 아님!)
   - 비밀번호: 테스트용 비밀번호
   - 이름: 테스트 사용자
   - 국가/지역: 대한민국
5. "초대" 클릭

---

## 3. 환경 변수 설정

### 3-1. 서버에 파일 업로드

#### Google Service Account 키 파일 업로드

1. SSH로 서버 접속:
   ```bash
   ssh -p 2222 async@asyncsite-server-2025.duckdns.org
   ```

2. secrets 디렉토리 생성:
   ```bash
   sudo mkdir -p /secrets
   sudo chmod 755 /secrets
   ```

3. 로컬에서 서버로 파일 전송:
   ```bash
   # 로컬 터미널에서 실행
   scp -P 2222 ~/Downloads/querydaily-iap-xxxxx-abc123.json \
     async@asyncsite-server-2025.duckdns.org:/tmp/

   # 서버에서 파일 이동
   ssh -p 2222 async@asyncsite-server-2025.duckdns.org
   sudo mv /tmp/querydaily-iap-xxxxx-abc123.json /secrets/iap-gateway-pubsub-key.json
   sudo chmod 600 /secrets/iap-gateway-pubsub-key.json
   ```

### 3-2. Docker Compose 환경 변수 설정

`docker-compose-iap-gateway.yml` 파일 수정:

```yaml
services:
  iap-gateway-service:
    image: asyncsite/iap-gateway-service:latest
    container_name: asyncsite-iap-gateway-service
    environment:
      # Google Pub/Sub (실제 값으로 변경)
      GOOGLE_CLOUD_PROJECT_ID: "querydaily-iap-xxxxx"  # 실제 Project ID
      GOOGLE_PUBSUB_SUBSCRIPTION: "iap-gateway-rtdn-sub"
      GOOGLE_APPLICATION_CREDENTIALS: "/secrets/iap-gateway-pubsub-key.json"
      PUBSUB_ENABLED: "true"  # Production에서 활성화

      # MySQL
      MYSQL_HOST: asyncsite-mysql
      MYSQL_PORT: 3306
      MYSQL_DATABASE: iap_gateway_db
      MYSQL_USERNAME: root
      MYSQL_PASSWORD: asyncsite_root_2024!

      # Redis
      SPRING_DATA_REDIS_HOST: asyncsite-redis
      SPRING_DATA_REDIS_PORT: 6379

      # Kafka
      KAFKA_BOOTSTRAP_SERVERS: asyncsite-kafka:9092

      # Payment Core
      PAYMENT_CORE_URL: http://asyncsite-payment-core:6082

      # Profile
      SPRING_PROFILES_ACTIVE: docker

    volumes:
      - /secrets:/secrets:ro  # secrets 디렉토리 마운트 (읽기 전용)
    ports:
      - "6084:6084"
    networks:
      - asyncsite-network
```

### 3-3. 환경 변수 확인

다음 정보를 메모장에 정리하세요:

```bash
# Google Cloud
GOOGLE_CLOUD_PROJECT_ID=querydaily-iap-xxxxx
GOOGLE_PUBSUB_SUBSCRIPTION=iap-gateway-rtdn-sub
GOOGLE_APPLICATION_CREDENTIALS=/secrets/iap-gateway-pubsub-key.json
PUBSUB_ENABLED=true

# Apple (설정만 필요, 환경변수 없음)
# Webhook URL: https://api.asyncsite.com/api/v1/iap/webhooks/ios

# 인앱 상품 ID (Google/Apple 공통)
com.asyncsite.querydaily.insights.100
com.asyncsite.querydaily.insights.500
com.asyncsite.querydaily.insights.1000
com.asyncsite.querydaily.insights.3000
```

---

## 4. 테스트 방법

### 4-1. 로컬 테스트 (Pub/Sub 비활성화)

1. 로컬 실행:
   ```bash
   cd ~/asyncsite/iap-gateway-service
   ./gradlew bootRun
   ```

2. Internal API 테스트:
   ```bash
   # IAPIntent 생성
   curl -X POST http://localhost:6084/internal/api/v1/iap/intents \
     -H "Content-Type: application/json" \
     -d '{
       "userEmail": "test@example.com",
       "productId": "com.asyncsite.querydaily.insights.500",
       "platform": "ANDROID"
     }'

   # Intent 조회
   curl http://localhost:6084/internal/api/v1/iap/intents/{intentId}
   ```

### 4-2. Google Play Sandbox 테스트

1. Android 앱 빌드 (Sandbox 모드)
2. 테스트 계정으로 로그인 (Google Play Console에서 추가한 계정)
3. 인앱 상품 구매 시도
4. 로그 확인:
   ```bash
   docker logs asyncsite-iap-gateway-service -f | grep GOOGLE
   ```

5. 예상 로그:
   ```
   [GOOGLE PLAY] Received Pub/Sub message: messageId=xxx
   [GOOGLE PLAY] Processing purchase notification: productId=com.asyncsite.querydaily.insights.500
   [GOOGLE PLAY] Notification processed successfully
   ```

### 4-3. Apple Sandbox 테스트

1. iOS 앱 빌드 (Sandbox 모드)
2. Sandbox 테스트 계정으로 로그인
3. 인앱 상품 구매 시도
4. 로그 확인:
   ```bash
   docker logs asyncsite-iap-gateway-service -f | grep APPLE
   ```

5. 예상 로그:
   ```
   [APPLE] Received server notification (signedPayload length: xxx)
   [APPLE JWT] Signature verified successfully
   [APPLE] Notification processed successfully
   ```

### 4-4. Webhook 수신 확인

**Apple Webhook 테스트:**
```bash
# App Store Connect에서 "Send Test Notification" 클릭
# 또는 curl로 테스트 (실제로는 Apple에서 전송)
curl -X POST https://api.asyncsite.com/api/v1/iap/webhooks/ios \
  -H "Content-Type: application/json" \
  -d '{
    "signedPayload": "eyJhbGciOiJFUzI1NiIsIng1YyI6W..."
  }'
```

**Google Pub/Sub 테스트:**
```bash
# Google Cloud Console에서 메시지 게시
# Pub/Sub → 주제 → asyncsite-iap-notifications → "메시지 게시" 클릭
```

---

## 5. 트러블슈팅

### 문제 1: Google Pub/Sub 연결 실패

**증상:**
```
Failed to start Pub/Sub subscriber: 401 Unauthorized
```

**해결:**
1. Service Account 키 파일 경로 확인
2. Service Account에 "Pub/Sub 구독자" 역할 부여 확인
3. 환경변수 `GOOGLE_APPLICATION_CREDENTIALS` 확인

### 문제 2: Apple Webhook 수신 안됨

**증상:**
- 구매했는데 로그에 아무것도 안 뜸

**해결:**
1. App Store Connect에서 Server URL 확인:
   - Production: `https://api.asyncsite.com/api/v1/iap/webhooks/ios`
   - Sandbox: `https://api-dev.asyncsite.com/api/v1/iap/webhooks/ios`
2. 서버가 HTTPS로 접근 가능한지 확인 (HTTP는 안됨!)
3. 방화벽에서 Apple IP 허용 확인

### 문제 3: 인앱 상품이 앱에서 안 보임

**증상:**
- 앱에서 상품 목록이 비어있음

**해결:**
1. Google Play Console / App Store Connect에서 상품 활성화 확인
2. 상품 ID가 코드와 정확히 일치하는지 확인
3. Sandbox 계정으로 로그인했는지 확인

---

## 6. 체크리스트

설정 완료 후 다음 항목들을 확인하세요:

### Google Play
- [ ] Google Cloud 프로젝트 생성
- [ ] Pub/Sub API 활성화
- [ ] Topic 생성 (`asyncsite-iap-notifications`)
- [ ] Subscription 생성 (`iap-gateway-rtdn-sub`)
- [ ] Service Account 생성 및 키 다운로드
- [ ] Service Account에 "Pub/Sub 구독자" 역할 부여
- [ ] Google Play Console에서 RTDN Topic 연결
- [ ] 인앱 상품 4개 등록 및 활성화
- [ ] 테스트 계정 추가

### Apple App Store
- [ ] App Store Connect에서 Server Notifications URL 설정
- [ ] 인앱 구매 상품 4개 등록 및 승인
- [ ] Sandbox 테스트 계정 생성

### 서버 설정
- [ ] Service Account 키 파일 업로드 (`/secrets/iap-gateway-pubsub-key.json`)
- [ ] Docker Compose 환경변수 설정
- [ ] IAP Gateway Service 빌드 및 배포
- [ ] Webhook 엔드포인트 HTTPS 접근 가능 확인

### 테스트
- [ ] 로컬 Internal API 테스트
- [ ] Google Play Sandbox 구매 테스트
- [ ] Apple Sandbox 구매 테스트
- [ ] Webhook 수신 로그 확인
- [ ] Kafka 이벤트 발행 확인

---

## 7. 참고 자료

- [Google Play 결제 문서](https://developer.android.com/google/play/billing)
- [Google Cloud Pub/Sub 문서](https://cloud.google.com/pubsub/docs)
- [Apple Server Notifications v2](https://developer.apple.com/documentation/appstoreservernotifications/app-store-server-notifications-v2)
- [Apple 인앱 구매 가이드](https://developer.apple.com/in-app-purchase/)

---

**문제가 발생하면 다음 정보를 포함하여 동민님께 문의하세요:**
1. 어떤 단계에서 막혔는지
2. 에러 메시지 전문
3. 로그 파일 (`docker logs asyncsite-iap-gateway-service`)
