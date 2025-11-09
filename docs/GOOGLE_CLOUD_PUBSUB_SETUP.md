# Google Cloud Pub/Sub Setup Guide (Phase 4)

> **Phase 4 Day 1**: Google Play Real-time Developer Notifications (RTDN) 설정

---

## 📋 사전 준비사항

- [ ] Google Cloud 계정
- [ ] Google Play Console 접근 권한
- [ ] gcloud CLI 설치 ([설치 가이드](https://cloud.google.com/sdk/docs/install))
- [ ] QueryDaily 앱이 Google Play Console에 등록됨

---

## 1. Google Cloud 프로젝트 생성

### Step 1: 프로젝트 생성

```bash
# Google Cloud Console에서 프로젝트 생성
# 1. https://console.cloud.google.com/ 접속
# 2. 프로젝트 선택 드롭다운 → "새 프로젝트"
# 3. 프로젝트 이름: "QueryDaily IAP"
# 4. 프로젝트 ID 기록 (예: querydaily-iap-xxxxx)
```

### Step 2: gcloud CLI 설정

```bash
# 프로젝트 설정
gcloud config set project querydaily-iap-xxxxx

# 현재 프로젝트 확인
gcloud config get-value project
```

---

## 2. Google Cloud Pub/Sub 설정

### Step 1: Pub/Sub API 활성화

```bash
# API 활성화
gcloud services enable pubsub.googleapis.com

# 활성화 확인
gcloud services list --enabled | grep pubsub
```

### Step 2: Pub/Sub Topic 생성

```bash
# Topic 생성 (Google Play가 메시지를 발행할 Topic)
gcloud pubsub topics create google-play-rtdn \
  --project=querydaily-iap-xxxxx

# Topic 확인
gcloud pubsub topics list --project=querydaily-iap-xxxxx

# 예상 출력:
# name: projects/querydaily-iap-xxxxx/topics/google-play-rtdn
```

### Step 3: Pub/Sub Subscription 생성 (Pull)

```bash
# IAP Gateway가 구독할 Subscription 생성
gcloud pubsub subscriptions create iap-gateway-rtdn-sub \
  --topic=google-play-rtdn \
  --ack-deadline=60 \
  --message-retention-duration=7d \
  --project=querydaily-iap-xxxxx

# Subscription 확인
gcloud pubsub subscriptions describe iap-gateway-rtdn-sub \
  --project=querydaily-iap-xxxxx

# 예상 출력:
# ackDeadlineSeconds: 60
# messageRetentionDuration: 604800s
# name: projects/querydaily-iap-xxxxx/subscriptions/iap-gateway-rtdn-sub
# topic: projects/querydaily-iap-xxxxx/topics/google-play-rtdn
```

**Subscription 설정 설명**:
- `--ack-deadline=60`: 메시지 처리 후 Ack를 보내야 하는 시간 (60초)
- `--message-retention-duration=7d`: 메시지를 7일간 보관

---

## 3. Service Account 생성 (IAP Gateway용)

### Step 1: Service Account 생성

```bash
# IAP Gateway가 Pub/Sub을 구독하기 위한 Service Account 생성
gcloud iam service-accounts create iap-gateway-pubsub \
  --display-name="IAP Gateway Pub/Sub Service Account" \
  --project=querydaily-iap-xxxxx

# Service Account 확인
gcloud iam service-accounts list --project=querydaily-iap-xxxxx

# 예상 출력:
# EMAIL: iap-gateway-pubsub@querydaily-iap-xxxxx.iam.gserviceaccount.com
```

### Step 2: Subscription에 대한 권한 부여

```bash
# Subscriber 권한 부여
gcloud pubsub subscriptions add-iam-policy-binding iap-gateway-rtdn-sub \
  --member="serviceAccount:iap-gateway-pubsub@querydaily-iap-xxxxx.iam.gserviceaccount.com" \
  --role="roles/pubsub.subscriber" \
  --project=querydaily-iap-xxxxx

# 권한 확인
gcloud pubsub subscriptions get-iam-policy iap-gateway-rtdn-sub \
  --project=querydaily-iap-xxxxx
```

### Step 3: JSON 키 다운로드

```bash
# JSON 키 생성 및 다운로드
gcloud iam service-accounts keys create iap-gateway-pubsub-key.json \
  --iam-account=iap-gateway-pubsub@querydaily-iap-xxxxx.iam.gserviceaccount.com \
  --project=querydaily-iap-xxxxx

# 키 파일 확인
ls -lh iap-gateway-pubsub-key.json

# ⚠️ 보안 경고: 이 파일은 절대 Git에 커밋하지 마세요!
# Kubernetes Secret 또는 환경변수로 주입해야 합니다.
```

**JSON 키 관리**:
```bash
# 로컬 개발용 (임시)
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/iap-gateway-pubsub-key.json

# Docker 환경
# docker-compose.yml에 volume으로 마운트
volumes:
  - ./secrets/iap-gateway-pubsub-key.json:/secrets/iap-gateway-pubsub-key.json

# Kubernetes 환경 (권장)
kubectl create secret generic iap-gateway-pubsub-key \
  --from-file=key.json=iap-gateway-pubsub-key.json \
  --namespace=default
```

---

## 4. Google Play Console RTDN 설정

### Step 1: Google Play Console 접속

1. [Google Play Console](https://play.google.com/console) 접속
2. 앱 선택: **QueryDaily**

### Step 2: Real-time Developer Notifications 설정

1. **수익 창출** → **수익 창출 설정**
2. **Google Cloud Pub/Sub** 섹션으로 이동
3. **주제 이름** 입력:
   ```
   projects/querydaily-iap-xxxxx/topics/google-play-rtdn
   ```
4. **변경사항 저장** 클릭

### Step 3: 권한 자동 부여 확인

Google Play가 Pub/Sub Topic에 메시지를 발행할 수 있도록 자동으로 권한이 부여됩니다:

```bash
# Topic의 IAM 정책 확인
gcloud pubsub topics get-iam-policy google-play-rtdn \
  --project=querydaily-iap-xxxxx

# 예상 출력에서 다음 Service Account 확인:
# - google-play-developer-notifications@system.gserviceaccount.com
# - role: roles/pubsub.publisher
```

---

## 5. 테스트: Pub/Sub 메시지 수동 발행

### Pub/Sub 메시지 발행 테스트

```bash
# 테스트 메시지 발행 (Base64 인코딩 필요)
gcloud pubsub topics publish google-play-rtdn \
  --message='{"version":"1.0","packageName":"com.asyncsite.querydaily","eventTimeMillis":"1234567890","oneTimeProductNotification":{"version":"1.0","notificationType":1,"purchaseToken":"test_token_123","sku":"com.asyncsite.querydaily.insights.500"}}' \
  --project=querydaily-iap-xxxxx

# 메시지 수신 테스트 (Pull)
gcloud pubsub subscriptions pull iap-gateway-rtdn-sub \
  --auto-ack \
  --limit=10 \
  --project=querydaily-iap-xxxxx
```

**예상 출력**:
```
┌─────────────────────────────────────────────────────────────────────┬──────────────────┬─────────────┬─────────┬──────────────────┐
│                                DATA                                 │    MESSAGE_ID    │ ORDERING_KEY│ ATTRIBUTES│ DELIVERY_ATTEMPT│
├─────────────────────────────────────────────────────────────────────┼──────────────────┼─────────────┼─────────┼──────────────────┤
│ {"version":"1.0","packageName":"com.asyncsite.querydaily",...}     │ 123456789        │             │         │                  │
└─────────────────────────────────────────────────────────────────────┴──────────────────┴─────────────┴─────────┴──────────────────┘
```

---

## 6. Service Account 설정 (Payment Core용)

Payment Core가 Google Play Developer API를 호출하려면 별도의 Service Account가 필요합니다.

### Step 1: Google Play Developer API 활성화

```bash
# API 활성화
gcloud services enable androidpublisher.googleapis.com --project=querydaily-iap-xxxxx

# 활성화 확인
gcloud services list --enabled | grep androidpublisher
```

### Step 2: Service Account 생성 (Payment Core용)

```bash
# Payment Core용 Service Account 생성
gcloud iam service-accounts create payment-core-google-play \
  --display-name="Payment Core Google Play API" \
  --project=querydaily-iap-xxxxx

# JSON 키 다운로드
gcloud iam service-accounts keys create payment-core-google-play-key.json \
  --iam-account=payment-core-google-play@querydaily-iap-xxxxx.iam.gserviceaccount.com \
  --project=querydaily-iap-xxxxx
```

### Step 3: Google Play Console에서 권한 부여

1. [Google Play Console](https://play.google.com/console) 접속
2. **설정** → **API 액세스**
3. **서비스 계정 연결** 클릭
4. Service Account 이메일 입력:
   ```
   payment-core-google-play@querydaily-iap-xxxxx.iam.gserviceaccount.com
   ```
5. **권한 부여**: "재무 데이터 보기" (필수)
6. **초대장 보내기** 클릭

---

## 7. 체크리스트

### Google Cloud 설정
- [ ] Google Cloud 프로젝트 생성
- [ ] Pub/Sub API 활성화
- [ ] Topic 생성 (`google-play-rtdn`)
- [ ] Subscription 생성 (`iap-gateway-rtdn-sub`)
- [ ] IAP Gateway Service Account 생성
- [ ] Subscriber 권한 부여
- [ ] JSON 키 다운로드

### Google Play Console 설정
- [ ] Google Play Console RTDN 설정
- [ ] Pub/Sub Topic 연결
- [ ] 권한 자동 부여 확인

### Payment Core 설정
- [ ] Google Play Developer API 활성화
- [ ] Payment Core Service Account 생성
- [ ] Google Play Console API 권한 부여
- [ ] JSON 키 Kubernetes Secret 등록

### 테스트
- [ ] Pub/Sub 메시지 발행 테스트
- [ ] Pub/Sub 메시지 수신 테스트

---

## 🐛 Troubleshooting

### 에러 1: "Permission denied" (Subscription)
**원인**: Service Account에 Subscriber 권한이 없음

**해결**:
```bash
gcloud pubsub subscriptions add-iam-policy-binding iap-gateway-rtdn-sub \
  --member="serviceAccount:iap-gateway-pubsub@querydaily-iap-xxxxx.iam.gserviceaccount.com" \
  --role="roles/pubsub.subscriber" \
  --project=querydaily-iap-xxxxx
```

### 에러 2: "Topic not found"
**원인**: Google Play Console에서 Topic 이름을 잘못 입력

**해결**: 다음 형식으로 정확히 입력
```
projects/querydaily-iap-xxxxx/topics/google-play-rtdn
```

### 에러 3: "The current user has insufficient permissions" (Payment Core)
**원인**: Payment Core Service Account에 "재무 데이터 보기" 권한이 없음

**해결**: Google Play Console → API 액세스 → 권한 부여

---

## 다음 단계

Day 1 완료 후:
- **Day 2로 이동**: IAP Gateway Pub/Sub Listener 구현
- **Day 3로 이동**: GooglePlayNotificationHandler 구현

---

**작성일**: 2025-11-09
**Phase**: 4 (Google Play Server Notification)
**Status**: ✅ Setup Guide Completed
