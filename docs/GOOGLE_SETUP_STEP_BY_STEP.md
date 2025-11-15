# Google Play IAP 설정 - 완전 초보자 가이드

> 🎯 목표: Google Service Account 키 파일(JSON) 다운로드 받기

---

## 전체 흐름 요약

```
1. Google Cloud Console 작업 (30분)
   └─> Service Account 키 (JSON 파일) 다운로드 ⭐

2. Google Play Console 작업 (20분)
   └─> Pub/Sub Topic 연결
   └─> 인앱 상품 4개 등록
```

---

## PART 1: Google Cloud Console (핵심! 여기서 키 파일 받음)

### Step 1: Google Cloud Console 접속

1. 브라우저 열기
2. 주소창에 입력: `https://console.cloud.google.com/`
3. Google 계정으로 로그인 (AsyncSite 회사 계정 추천)

### Step 2: 새 프로젝트 만들기

화면 상단을 보세요:

```
┌─────────────────────────────────────────────────┐
│ ☰  Google Cloud Platform  │ [프로젝트 선택 ▼] │
└─────────────────────────────────────────────────┘
```

**이렇게 하세요:**

1. **상단 가운데** "프로젝트 선택" (또는 "Select a project") 클릭
   - 드롭다운 메뉴가 나타남

2. 팝업 창 **오른쪽 위** "새 프로젝트" (NEW PROJECT) 클릭

3. 프로젝트 정보 입력:
   ```
   프로젝트 이름: querydaily-iap-production
   조직: (비워두거나 회사 선택)
   위치: (비워두거나 회사 선택)
   ```

4. **"만들기" (CREATE)** 버튼 클릭

5. ⏱️ 잠시 기다리면 프로젝트 생성 완료

6. ⭐ **중요: 프로젝트 ID를 메모장에 복사하세요!**
   - 형식: `querydaily-iap-production` 또는 `querydaily-iap-xxxxx`
   - 화면 어딘가에 "프로젝트 ID: querydaily-iap-xxxxx" 보일 거예요

### Step 3: Pub/Sub API 활성화

**왼쪽 메뉴(☰)에서:**

1. 왼쪽 위 **햄버거 메뉴(☰)** 클릭
2. "API 및 서비스" 찾기 → 마우스 올리기
3. → "라이브러리" 클릭

**API 검색:**

1. 화면 상단에 검색창이 있음: "API 및 서비스 검색"
2. 검색창에 입력: `Pub/Sub`
3. "Cloud Pub/Sub API" 클릭 (정확한 이름!)
4. **"사용 설정" (ENABLE)** 버튼 클릭
5. ⏱️ 잠시 기다리면 활성화 완료

### Step 4: Pub/Sub Topic 생성

**왼쪽 메뉴로 이동:**

1. 왼쪽 위 **햄버거 메뉴(☰)** 클릭
2. 쭉 내리면 "Pub/Sub" 찾기
3. "Pub/Sub" 클릭
4. → "주제" (Topics) 클릭

**Topic 만들기:**

1. 화면 상단 **"주제 만들기" (CREATE TOPIC)** 버튼 클릭

2. 팝업이 뜸 - 다음 입력:
   ```
   주제 ID: asyncsite-iap-notifications

   기타 설정은 건드리지 마세요! (기본값 그대로)
   ```

3. **"만들기" (CREATE)** 버튼 클릭

4. ⭐ **중요: 전체 Topic 이름을 메모장에 복사하세요!**
   - 화면에 이렇게 보임:
   ```
   projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications
   ```
   - 전체를 복사! (나중에 Google Play Console에서 사용)

### Step 5: Subscription 만들기

**방금 만든 Topic 화면에서:**

1. Topic 이름 (`asyncsite-iap-notifications`) 클릭

2. 화면 상단 **"구독 만들기" (CREATE SUBSCRIPTION)** 버튼 클릭

3. 구독 정보 입력:
   ```
   구독 ID: iap-gateway-rtdn-sub

   전송 유형: Pull ⚠️ 중요! (Push 아님)

   메시지 보관 기간: 7일

   승인 기한: 60초

   재시도 정책: 즉시 재시도

   기타 설정: 기본값 그대로
   ```

4. **"만들기" (CREATE)** 버튼 클릭

### Step 6: Service Account 만들기 (중요!)

**왼쪽 메뉴로 이동:**

1. 왼쪽 위 **햄버거 메뉴(☰)** 클릭
2. "IAM 및 관리자" 찾기 → 마우스 올리기
3. → "서비스 계정" (Service Accounts) 클릭

**Service Account 생성:**

1. 화면 상단 **"서비스 계정 만들기" (CREATE SERVICE ACCOUNT)** 버튼 클릭

2. **1단계: 서비스 계정 세부정보**
   ```
   서비스 계정 이름: iap-gateway-pubsub-reader

   서비스 계정 ID: (자동 생성됨 - 건드리지 마세요)

   설명: IAP Gateway Pub/Sub Reader (선택사항)
   ```
   - **"만들고 계속하기" (CREATE AND CONTINUE)** 클릭

3. **2단계: 역할 부여** ⭐ 중요!
   ```
   역할 선택 드롭다운 클릭

   검색창에 입력: "Pub/Sub 구독자"

   "Pub/Sub 구독자" (Pub/Sub Subscriber) 선택
   ```
   - **"계속" (CONTINUE)** 클릭

4. **3단계: 사용자 액세스 권한**
   - 아무것도 입력하지 않고
   - **"완료" (DONE)** 클릭

### Step 7: Service Account 키 다운로드 (최종 목표!)

**서비스 계정 목록 화면에서:**

1. 방금 만든 서비스 계정 찾기:
   ```
   iap-gateway-pubsub-reader@querydaily-iap-xxxxx.iam.gserviceaccount.com
   ```

2. 이메일 주소 **클릭** (전체 행 클릭)

3. 상단 탭 중 **"키" (KEYS)** 탭 클릭

4. **"키 추가" (ADD KEY)** 버튼 클릭
   - 드롭다운에서 "새 키 만들기" (Create new key) 선택

5. 팝업이 뜸:
   ```
   키 유형: JSON ⚠️ 반드시 JSON 선택! (P12 아님)
   ```

6. **"만들기" (CREATE)** 버튼 클릭

7. 🎉 **JSON 파일 자동 다운로드!**
   - 파일명 예시: `querydaily-iap-production-a1b2c3d4e5f6.json`
   - 다운로드 폴더에 저장됨

8. ⭐ **이 파일이 바로 우리가 필요한 Service Account 키입니다!**
   - 안전한 곳에 보관하세요
   - 절대 GitHub에 올리지 마세요!
   - 나중에 서버에 업로드할 거예요

### ✅ Google Cloud Console 작업 완료!

**메모장에 기록한 정보 확인:**

```
✅ 프로젝트 ID: querydaily-iap-xxxxx
✅ Topic 전체 이름: projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications
✅ Subscription ID: iap-gateway-rtdn-sub
✅ JSON 키 파일 다운로드: querydaily-iap-production-a1b2c3d4e5f6.json
```

---

## PART 2: Google Play Console

### 사전 준비

- QueryDaily 앱이 Google Play Console에 등록되어 있어야 함
- 등록 안되어 있으면 먼저 앱 등록 필요

### Step 1: Google Play Console 접속

1. 브라우저에서 `https://play.google.com/console` 접속
2. Google 계정 로그인 (개발자 계정)
3. QueryDaily 앱 선택

### Step 2: Real-time Developer Notifications (RTDN) 설정

**왼쪽 메뉴에서:**

1. "수익 창출" (Monetization) 찾기
2. → "수익 창출 설정" (Monetization setup) 클릭

**Cloud Pub/Sub 알림 설정:**

1. 페이지를 쭉 내리면 "Cloud Pub/Sub 알림" 섹션 찾기

2. "주제 이름" 입력란에 다음 붙여넣기:
   ```
   projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications
   ```
   (위에서 복사한 전체 Topic 이름!)

3. **"주제 이름 추가" 버튼 클릭**

4. 화면 아래로 스크롤 → **"변경사항 저장"** 클릭

### Step 3: 인앱 상품 등록

**왼쪽 메뉴에서:**

1. "수익 창출" → "상품" → "인앱 상품" 클릭

2. **"상품 만들기" (Create product)** 버튼 클릭

**상품 1: Insights 100개**

```
상품 ID: com.asyncsite.querydaily.insights.100
이름: Insights 100개
설명: 면접 답변 인사이트 100개를 획득합니다
상태: 활성
가격: $1.09 (또는 ₩1,500)
```

- **"저장"** 클릭
- **"활성화"** 클릭

**상품 2: Insights 500개**

```
상품 ID: com.asyncsite.querydaily.insights.500
이름: Insights 500개
설명: 면접 답변 인사이트 500개를 획득합니다
상태: 활성
가격: $4.99 (또는 ₩6,500)
```

- **"저장"** → **"활성화"**

**상품 3: Insights 1000개**

```
상품 ID: com.asyncsite.querydaily.insights.1000
이름: Insights 1000개
설명: 면접 답변 인사이트 1000개를 획득합니다
상태: 활성
가격: $8.99 (또는 ₩11,900)
```

- **"저장"** → **"활성화"**

**상품 4: Insights 3000개**

```
상품 ID: com.asyncsite.querydaily.insights.3000
이름: Insights 3000개
설명: 면접 답변 인사이트 3000개를 획득합니다
상태: 활성
가격: $24.99 (또는 ₩32,900)
```

- **"저장"** → **"활성화"**

### Step 4: 테스트 계정 추가 (선택사항)

**왼쪽 메뉴에서:**

1. "설정" → "라이선스 테스트" 클릭

2. **"라이선스 테스터 추가"** 클릭

3. Gmail 계정 입력 (예: `yourtest@gmail.com`)

4. **"저장"** 클릭

---

## ✅ 전체 완료 체크리스트

### Google Cloud Console
- [ ] 프로젝트 생성 (`querydaily-iap-production`)
- [ ] Pub/Sub API 활성화
- [ ] Topic 생성 (`asyncsite-iap-notifications`)
- [ ] Subscription 생성 (`iap-gateway-rtdn-sub`, Pull 방식)
- [ ] Service Account 생성 (`iap-gateway-pubsub-reader`)
- [ ] Service Account에 "Pub/Sub 구독자" 역할 부여
- [ ] **JSON 키 파일 다운로드** ⭐

### Google Play Console
- [ ] RTDN Topic 연결
- [ ] 인앱 상품 4개 등록 및 활성화
  - [ ] com.asyncsite.querydaily.insights.100 ($1.09)
  - [ ] com.asyncsite.querydaily.insights.500 ($4.99)
  - [ ] com.asyncsite.querydaily.insights.1000 ($8.99)
  - [ ] com.asyncsite.querydaily.insights.3000 ($24.99)
- [ ] 테스트 계정 추가 (선택사항)

---

## 📝 최종 확인

**메모장에 다음 정보가 있어야 합니다:**

```
✅ Google Cloud Project ID: querydaily-iap-xxxxx
✅ Pub/Sub Topic: projects/querydaily-iap-xxxxx/topics/asyncsite-iap-notifications
✅ Subscription ID: iap-gateway-rtdn-sub
✅ JSON 키 파일: querydaily-iap-production-a1b2c3d4e5f6.json (다운로드됨)
```

**다운로드한 JSON 파일 위치:**
- Mac: `~/Downloads/querydaily-iap-production-xxxxx.json`
- Windows: `C:\Users\YourName\Downloads\querydaily-iap-production-xxxxx.json`

---

## 🚀 다음 단계

1. 다운로드한 JSON 파일을 서버에 업로드
2. Apple App Store 설정 (별도 가이드)
3. 테스트

---

## ❓ 문제 해결

### Q1: "프로젝트 선택" 버튼이 안 보여요
- 화면 상단 중앙을 보세요
- "Google Cloud Platform" 로고 옆에 있어야 함
- 새로고침 해보세요

### Q2: Pub/Sub가 메뉴에 없어요
- API를 활성화했는지 확인
- 프로젝트가 올바르게 선택되었는지 확인 (상단 확인)

### Q3: Service Account 역할을 못 찾겠어요
- 역할 선택 드롭다운을 클릭
- 검색창에 정확히 입력: `Pub/Sub 구독자`
- 영어로는: `Pub/Sub Subscriber`

### Q4: JSON 파일이 다운로드 안돼요
- 팝업 차단 해제
- 브라우저 설정 확인
- 다운로드 폴더 확인

### Q5: Google Play Console에서 앱이 안 보여요
- 먼저 앱을 등록해야 함
- 개발자 계정이 필요함
- 계정에 앱 접근 권한이 있는지 확인

---

**🎯 여기까지 완료하면 Google 설정 끝!**

다음은 Apple App Store 설정입니다.
