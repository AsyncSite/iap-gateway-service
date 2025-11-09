# Apple App Store Connect 설정 가이드

> **작성일**: 2025-11-09
> **Phase 5**: Apple Server Notifications v2
> **참고**: https://developer.apple.com/documentation/appstoreservernotifications

---

## 📋 목차

1. [인앱 상품 등록](#1-인앱-상품-등록)
2. [App Store Server Notifications v2 URL 설정](#2-app-store-server-notifications-v2-url-설정)
3. [Sandbox 테스터 계정 생성](#3-sandbox-테스터-계정-생성)
4. [Troubleshooting](#4-troubleshooting)

---

## 1. 인앱 상품 등록

### 1.1 App Store Connect 접속

1. [App Store Connect](https://appstoreconnect.apple.com/) 로그인
2. "내 앱" → **QueryDaily** 선택
3. "인앱 구입" 탭 클릭

### 1.2 상품 등록 (4개)

#### 상품 1: 인사이트 100개

```
제품 ID: com.asyncsite.querydaily.insights.100
참조 이름: 인사이트 100개
유형: 소모품 (Consumable)
가격: Tier 1 ($0.99, ₩1,200)
설명: "인사이트 100개를 구매하여 AI 분석 보고서를 생성하세요"
```

#### 상품 2: 인사이트 500개

```
제품 ID: com.asyncsite.querydaily.insights.500
참조 이름: 인사이트 500개
유형: 소모품 (Consumable)
가격: Tier 5 ($4.99, ₩6,500)
설명: "인사이트 500개를 구매하여 AI 분석 보고서를 생성하세요"
```

#### 상품 3: 인사이트 1000개

```
제품 ID: com.asyncsite.querydaily.insights.1000
참조 이름: 인사이트 1000개
유형: 소모품 (Consumable)
가격: Tier 10 ($9.99, ₩12,000)
설명: "인사이트 1000개를 구매하여 AI 분석 보고서를 생성하세요"
```

#### 상품 4: 인사이트 3000개

```
제품 ID: com.asyncsite.querydaily.insights.3000
참조 이름: 인사이트 3000개
유형: 소모품 (Consumable)
가격: Tier 25 ($24.99, ₩33,000)
설명: "인사이트 3000개를 구매하여 AI 분석 보고서를 생성하세요"
```

### 1.3 상품 제출 및 승인

1. 각 상품의 "제출" 버튼 클릭
2. Apple 심사 대기 (보통 24-48시간)
3. 심사 승인 확인

**⚠️ 중요**: Sandbox 환경에서는 승인 전에도 테스트 가능합니다.

---

## 2. App Store Server Notifications v2 URL 설정

### 2.1 Server Notification URL 등록

1. [App Store Connect](https://appstoreconnect.apple.com/) 로그인
2. "내 앱" → **QueryDaily** 선택
3. "앱 정보" 탭 클릭
4. "App Store Server Notifications" 섹션으로 스크롤

### 2.2 Production Server URL 입력

**Production Server URL**:
```
https://api.asyncsite.com/api/v1/iap/webhooks/ios
```

**설정 방법**:
1. "Production Server URL" 필드에 위 URL 입력
2. "Version 2"를 선택 (필수!)
3. "저장" 버튼 클릭

### 2.3 Sandbox Server URL 입력

**Sandbox Server URL**:
```
https://dev-api.asyncsite.com/api/v1/iap/webhooks/ios
```

**설정 방법**:
1. "Sandbox Server URL" 필드에 위 URL 입력
2. "Version 2"를 선택 (필수!)
3. "저장" 버튼 클릭

### 2.4 URL 요구 사항

**필수 조건**:
- ✅ HTTPS 프로토콜 (HTTP 불가)
- ✅ 유효한 SSL/TLS 인증서
- ✅ Public 접근 가능 (VPN 내부 금지)
- ✅ 2xx HTTP 상태 코드 응답 (200 OK 권장)

**검증 방법**:
```bash
# URL 접근성 테스트
curl -X POST https://api.asyncsite.com/api/v1/iap/webhooks/ios \
  -H "Content-Type: application/json" \
  -d '{"test": "ping"}'

# 예상 응답: 200 OK
```

---

## 3. Sandbox 테스터 계정 생성

### 3.1 Sandbox 테스터 추가

1. [App Store Connect](https://appstoreconnect.apple.com/) 로그인
2. "사용자 및 액세스" 메뉴 클릭
3. 왼쪽 사이드바에서 "Sandbox" → "테스터" 클릭
4. "추가" (+) 버튼 클릭

### 3.2 테스터 정보 입력

**테스터 1**:
```
이메일: ios-tester1@asyncsite.com
비밀번호: [강력한 비밀번호 설정]
이름: iOS
성: Tester1
국가/지역: 대한민국
```

**테스터 2** (선택사항):
```
이메일: ios-tester2@asyncsite.com
비밀번호: [강력한 비밀번호 설정]
이름: iOS
성: Tester2
국가/지역: 미국
```

### 3.3 테스터 계정 사용 방법

**iOS 기기에서 로그아웃**:
1. 설정 → App Store → Apple ID 탭
2. "로그아웃" 클릭

**Sandbox 테스터로 로그인**:
1. TestFlight 앱 또는 QueryDaily 앱 실행
2. 구매 시도 시 로그인 팝업 표시
3. Sandbox 테스터 계정 입력: `ios-tester1@asyncsite.com`
4. 구매 진행

**⚠️ 중요**:
- Sandbox 테스터 계정은 **앱 구매 시에만** 사용
- 설정 앱에서는 실제 Apple ID 유지

---

## 4. Troubleshooting

### 4.1 Server Notification이 수신되지 않음

**증상**:
- iOS 앱에서 구매 완료
- IAP Gateway 로그에 아무것도 없음

**원인 및 해결책**:

#### 1. URL 접근성 문제
```bash
# 외부에서 접근 가능한지 확인
curl -X POST https://api.asyncsite.com/api/v1/iap/webhooks/ios \
  -H "Content-Type: application/json" \
  -d '{"signedPayload": "test"}'

# 200 OK 응답이 와야 함
```

#### 2. SSL 인증서 문제
```bash
# SSL 인증서 유효성 확인
openssl s_client -connect api.asyncsite.com:443 -servername api.asyncsite.com

# Verify return code: 0 (ok) 확인
```

#### 3. App Store Connect 설정 누락
- App Store Connect → 앱 정보 → App Store Server Notifications 확인
- Version 2 선택 확인
- URL 오타 확인

#### 4. Sandbox 환경 불일치
- Sandbox 구매는 Sandbox Server URL로 전송
- Production 구매는 Production Server URL로 전송
- 환경 확인 필수

---

### 4.2 JWT 검증 실패

**증상**:
```
[APPLE] JWT verification failed: JWT signature verification failed
```

**원인 및 해결책**:

#### 1. Apple 공개 키 다운로드 실패
```bash
# Apple 공개 키 URL 확인
curl https://appleid.apple.com/auth/keys

# JSON 응답 확인
```

#### 2. JWT 형식 오류
- `signedPayload` 필드가 JWT 형식인지 확인
- `eyJ...` 로 시작하는지 확인

#### 3. 시스템 시간 불일치
```bash
# 서버 시간 확인
date

# NTP 동기화
sudo ntpdate -u time.apple.com
```

---

### 4.3 Intent not found 에러

**증상**:
```
[APPLE] Intent not found: intent_xxx
```

**원인 및 해결책**:

#### 1. appAccountToken 누락
- iOS 앱에서 구매 시 `appAccountToken` 전달 확인
- StoreKit 2: `Product.PurchaseOption.appAccountToken(UUID)`

```swift
// Swift 예시
let result = try await product.purchase(
    options: [.appAccountToken(UUID(uuidString: intentId)!)]
)
```

#### 2. Intent 생성 안 됨
- 구매 전 `POST /api/v1/iap/intents` 호출 확인
- Response에서 `intentId` 받기 확인

#### 3. Intent 만료
- Intent TTL: 10분
- 10분 이내에 구매 완료 필요

---

### 4.4 중복 구매 처리

**증상**:
- 같은 구매에 대해 Server Notification 2번 수신
- InsightWallet 중복 충전

**원인**:
- Apple은 같은 이벤트를 재시도할 수 있음
- 멱등성 보장 필요

**해결책**:
```java
// GooglePlayNotificationHandler.java 참고
if (intent.getStatus() == IAPIntentStatus.VERIFIED) {
    log.info("[APPLE] Intent already verified, skipping duplicate notification");
    return;
}
```

---

### 4.5 로그 확인 명령어

**IAP Gateway 로그**:
```bash
# 전체 로그
docker logs asyncsite-iap-gateway-service --tail 100

# Apple 관련 로그만
docker logs asyncsite-iap-gateway-service | grep "APPLE"

# 실시간 로그
docker logs -f asyncsite-iap-gateway-service
```

**Payment Core 로그**:
```bash
# Apple 관련 로그
docker logs asyncsite-payment-core | grep "APPLE"
```

**Kafka 이벤트 확인**:
```bash
docker exec -it asyncsite-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic asyncsite.payment.verified \
  --from-beginning
```

**MySQL 데이터 확인**:
```bash
# IAP Gateway DB
docker exec -it asyncsite-mysql mysql -uroot -pasyncsite_root_2024! iap_gateway_db

# Intent 조회
SELECT * FROM iap_intents WHERE status = 'VERIFIED' ORDER BY created_at DESC LIMIT 10;
```

---

## 5. 참고 자료

### Apple 공식 문서
- [App Store Server Notifications V2](https://developer.apple.com/documentation/appstoreservernotifications/app-store-server-notifications-v2)
- [Enabling App Store Server Notifications](https://developer.apple.com/documentation/appstoreservernotifications/enabling-app-store-server-notifications)
- [Receiving App Store Server Notifications](https://developer.apple.com/documentation/appstoreservernotifications/receiving-app-store-server-notifications)
- [ResponseBodyV2DecodedPayload](https://developer.apple.com/documentation/appstoreservernotifications/responsebodyv2decodedpayload)
- [JWSTransactionDecodedPayload](https://developer.apple.com/documentation/appstoreserverapi/jwstransactiondecodedpayload)

### 내부 문서
- [IAP_PHASE_5_APPLE.md](../../../checkout-service/docs/plan/IAP_PHASE_5_APPLE.md)
- [CLAUDE.md](../CLAUDE.md)

---

## 6. 체크리스트

### App Store Connect 설정
- [ ] 인앱 상품 4개 등록 완료
- [ ] Production Server Notification URL 설정
- [ ] Sandbox Server Notification URL 설정
- [ ] Sandbox 테스터 계정 2개 생성

### URL 검증
- [ ] HTTPS 접근성 확인
- [ ] SSL 인증서 유효성 확인
- [ ] 2xx 응답 코드 확인

### 테스트 준비
- [ ] TestFlight 빌드 배포
- [ ] Sandbox 테스터 로그인 테스트
- [ ] 구매 플로우 확인

---

**작성자**: IAP Gateway Team
**검토자**: -
**승인자**: -
**최종 업데이트**: 2025-11-09
