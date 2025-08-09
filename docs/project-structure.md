# 프로젝트 구조 및 개발 가이드

## 📁 폴더 구조 상세

### `android/` - 안드로이드 앱

```
android/
├── app/src/main/java/com/shinhan/ble/
│   ├── MainActivity.kt                 # 메인 화면 (BLE 스캔/광고)
│   ├── AccountSelectionActivity.kt     # 계좌 선택 화면
│   ├── TransferAmountActivity.kt       # 송금 금액 입력 화면
│   ├── TransferConfirmationActivity.kt # 송금 확인 화면
│   │
│   ├── advertiser/
│   │   └── BleAdvertiser.kt           # BLE 광고 (내 송금코드 브로드캐스트)
│   │
│   ├── scanner/
│   │   ├── BleScanner.kt              # BLE 스캔 인터페이스
│   │   ├── RealBleScanner.kt          # 실제 BLE 스캔 구현
│   │   └── MockBleScanner.kt          # 테스트용 Mock 스캔
│   │
│   ├── data/
│   │   ├── ShinhanTransferCode.kt     # 신한은행 송금코드 데이터 클래스
│   │   ├── ShinhanBLEData.kt          # BLE 브로드캐스트용 데이터
│   │   ├── ScannedUser.kt             # 스캔된 사용자 정보
│   │   │
│   │   ├── database/                  # 로컬 데이터베이스 (Room)
│   │   │   ├── entities/              # 데이터 엔티티
│   │   │   ├── dao/                   # 데이터 접근 객체
│   │   │   └── BleTransferRepository.kt # Repository 패턴
│   │   │
│   │   └── network/                   # 서버 통신
│   │       ├── api/ShinhanApiService.kt    # Retrofit API 인터페이스
│   │       ├── dto/ShinhanApiResponse.kt   # 데이터 전송 객체
│   │       ├── interceptor/                # HTTP 인터셉터
│   │       └── repository/ShinhanApiRepository.kt
│   │
│   └── di/                            # 의존성 주입 (Hilt)
│       ├── NetworkModule.kt           # 네트워크 모듈
│       ├── DatabaseModule.kt          # 데이터베이스 모듈
│       └── ScannerModule.kt           # BLE 스캐너 모듈
```

### `backend/` - Spring Boot 백엔드 서버

```
backend/
├── src/main/kotlin/com/shinhan/ble/
│   ├── BleBackendApplication.kt       # 메인 애플리케이션
│   │
│   ├── controller/                    # REST API 컨트롤러
│   │   ├── AccountController.kt       # 계좌 관련 API
│   │   ├── TransferController.kt      # 송금 관련 API
│   │   └── AuthController.kt          # 인증 관련 API
│   │
│   ├── service/                       # 비즈니스 로직
│   │   ├── AccountService.kt          # 계좌 서비스
│   │   ├── TransferService.kt         # 송금 서비스
│   │   └── AuthService.kt             # 인증 서비스
│   │
│   ├── entity/                        # JPA 엔티티
│   │   ├── Customer.kt                # 고객 정보
│   │   ├── Account.kt                 # 계좌 정보
│   │   └── Transfer.kt                # 송금 내역
│   │
│   ├── repository/                    # 데이터 접근 계층
│   │   ├── CustomerRepository.kt      # 고객 Repository
│   │   ├── AccountRepository.kt       # 계좌 Repository
│   │   └── TransferRepository.kt      # 송금 Repository
│   │
│   ├── dto/                          # 데이터 전송 객체
│   │   ├── request/                   # 요청 DTO
│   │   └── response/                  # 응답 DTO
│   │
│   ├── config/                        # 설정 클래스
│   │   ├── WebConfig.kt               # 웹 설정
│   │   └── DatabaseConfig.kt          # 데이터베이스 설정
│   │
│   └── security/                      # 보안 설정
│       ├── SecurityConfig.kt          # Spring Security 설정
│       ├── JwtTokenProvider.kt        # JWT 토큰 관리
│       └── JwtAuthenticationFilter.kt # JWT 인증 필터
│
└── src/main/resources/
    ├── application.yml                # 설정 파일
    └── data.sql                       # 초기 데이터
```

### `shared/` - 공통 모델

```
shared/
└── models/
    └── ShinhanApiModels.kt           # 앱↔서버 공통 데이터 모델
```

### `docs/` - 문서

```
docs/
├── api-spec.md                       # REST API 명세서
├── architecture.md                   # 시스템 아키텍처
├── deployment.md                     # 배포 가이드
└── project-structure.md              # 이 파일
```

## 🔄 데이터 흐름

### 1. BLE 스캔 및 송금코드 교환

```
[안드로이드 A] ──BLE Advertise──> [송금코드: 088SH123ABC...]
[안드로이드 B] ──BLE Scanner──────> [송금코드 수신 및 파싱]
```

### 2. 서버를 통한 실제 송금

```
[안드로이드 B] ──HTTP POST──> [백엔드 서버] ──DB 조회──> [송금코드 검증]
                                    ↓
[신한은행 코어뱅킹] ←─────────────> [실제 계좌이체 처리]
                                    ↓
[안드로이드 B] ←────HTTP Response─── [송금 결과 반환]
```

## 🛠️ 개발 환경 설정

### 안드로이드 앱 개발

1. **Android Studio 설정**
   ```bash
   cd android
   ./gradlew assembleDebug
   ```

2. **필수 권한**
   - `BLUETOOTH_SCAN`
   - `BLUETOOTH_ADVERTISE`
   - `ACCESS_FINE_LOCATION`
   - `INTERNET`

### 백엔드 서버 개발

1. **Spring Boot 실행**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

2. **개발 데이터베이스**: H2 인메모리 DB
   - 콘솔: http://localhost:8080/api/v1/h2-console
   - URL: `jdbc:h2:mem:testdb`
   - 사용자: `sa`
   - 비밀번호: (없음)

## 🔧 빌드 및 배포

### 안드로이드 빌드

```bash
cd android
./gradlew assembleRelease     # Release APK 생성
./gradlew bundleRelease       # AAB 생성 (Google Play)
```

### 백엔드 빌드

```bash
cd backend
./gradlew bootJar            # 실행 가능한 JAR 생성
```

### Docker 배포 (향후 계획)

```bash
cd backend
docker build -t shinhan-ble-backend .
docker run -p 8080:8080 shinhan-ble-backend
```

## 🧪 테스트

### 안드로이드 테스트

```bash
cd android
./gradlew test                # 단위 테스트
./gradlew connectedAndroidTest # 통합 테스트
```

### 백엔드 테스트

```bash
cd backend
./gradlew test                # 모든 테스트 실행
./gradlew testDebugUnitTest   # 단위 테스트만
```

## 📊 모니터링 및 로깅

### 안드로이드 로그

```kotlin
Log.d("BleAdvertiser", "Broadcasting transfer code: $transferCode")
Log.e("ShinhanApi", "Transfer failed: ${error.message}")
```

### 백엔드 로그

```kotlin
logger.info("Transfer executed: $transactionId")
logger.error("Validation failed for code: $transferCode")
```

## 🔒 보안 고려사항

### 안드로이드
- BLE 데이터 AES 암호화
- HTTPS 통신 강제
- JWT 토큰 안전한 저장 (EncryptedSharedPreferences)

### 백엔드
- Spring Security + JWT
- SQL Injection 방지 (JPA Parameterized Query)
- CORS 설정
- Rate Limiting

## 📈 성능 최적화

### 안드로이드
- BLE 스캔 배치 처리 (5초 간격)
- 네트워크 요청 캐싱
- Room 데이터베이스 인덱싱

### 백엔드
- 데이터베이스 커넥션 풀링
- JPA 쿼리 최적화
- 레디스 캐싱 (향후 계획)

이 구조를 통해 안드로이드 앱과 백엔드 서버를 독립적으로 개발하면서도 효과적으로 연동할 수 있습니다.