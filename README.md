# SOL BLE Transfer System

> **BLE를 이용한 신한은행 쏠(SOL) 계좌 이체 서비스**  
> 안드로이드 앱 + 백엔드 서버로 구성된 완전한 BLE 기반 P2P 송금 시스템

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)

## 📚 문서

- [요구사항 명세서](./docs/Requirements_specification.md)
- [실제 시연 영상](./docs/Demo_Video.md)

## 📁 프로젝트 구조

```
SOL-BLE-Transfer/
├── android/                          # 안드로이드 앱
│   ├── app/src/main/java/com/shinhan/ble/
│   │   ├── MainActivity.kt           # 메인 BLE 스캔 화면
│   │   ├── AccountSelectionActivity.kt # 계좌 선택 화면
│   │   ├── TransferAmountActivity.kt   # 송금 금액 입력 화면
│   │   ├── TransferConfirmationActivity.kt # 송금 확인 화면
│   │   ├── TransferResultActivity.kt   # 송금 결과 화면
│   │   ├── ProximityVisualizationView.kt # 근거리 사용자 시각화 뷰
│   │   ├── advertiser/
│   │   │   └── BleAdvertiser.kt      # BLE 광고 관리
│   │   ├── scanner/
│   │   │   ├── BleScanner.kt         # BLE 스캔 인터페이스
│   │   │   ├── RealBleScanner.kt     # 실제 BLE 스캔 구현
│   │   │   └── MockBleScanner.kt     # 테스트용 Mock 스캔
│   │   ├── data/
│   │   │   ├── ScannedUser.kt        # 스캔된 사용자 모델
│   │   │   ├── ShinhanBLEData.kt     # BLE 데이터 모델
│   │   │   ├── ShinhanTransferCode.kt # BLE 송금코드 모델
│   │   │   ├── network/
│   │   │   │   ├── api/ShinhanApiService.kt # REST API 인터페이스
│   │   │   │   ├── dto/ShinhanApiResponse.kt # API 응답 DTO
│   │   │   │   └── interceptor/      # HTTP 인터셉터
│   │   │   │       ├── AuthInterceptor.kt
│   │   │   │       └── ErrorInterceptor.kt
│   │   │   └── repository/
│   │   │       ├── ShinhanApiRepository.kt # API 레포지토리
│   │   │       └── BleTransferRepository.kt # BLE 전송 레포지토리
│   │   ├── di/
│   │   │   ├── NetworkModule.kt      # 네트워크 의존성 주입
│   │   │   └── ScannerModule.kt      # 스캐너 의존성 주입
│   │   └── utils/
│   │       ├── DeviceInfoHelper.kt   # 디바이스 정보 유틸
│   │       └── FirstLaunchHelper.kt  # 첫 실행 처리
│   ├── app/src/main/res/
│   │   ├── layout/                   # UI 레이아웃
│   │   │   ├── activity_main.xml     # 메인 화면
│   │   │   ├── activity_account_selection.xml
│   │   │   ├── activity_transfer_amount.xml
│   │   │   ├── activity_transfer_confirmation.xml
│   │   │   ├── activity_transfer_result.xml
│   │   │   ├── item_account.xml      # 계좌 아이템
│   │   │   ├── item_scanned_user.xml # 스캔된 사용자 아이템
│   │   │   └── item_device.xml       # 디바이스 아이템
│   │   ├── drawable/                 # 아이콘 및 그래픽 리소스
│   │   │   ├── sol_header_gradient.xml
│   │   │   ├── ic_bluetooth.xml
│   │   │   ├── ic_security.xml
│   │   │   ├── ic_shield.xml
│   │   │   ├── ic_verified.xml
│   │   │   └── rounded_button.xml
│   │   └── values/
│   │       ├── colors.xml            # 앱 색상 정의
│   │       ├── strings.xml           # 문자열 리소스
│   │       └── themes.xml            # 앱 테마
│   └── build.gradle.kts
│
├── backend/                          # Spring Boot 백엔드 서버
│   └── src/main/kotlin/com/shinhan/ble/
│       ├── BleBackendApplication.kt  # 메인 애플리케이션
│       ├── controller/
│       │   ├── AccountController.kt  # 계좌 관련 API
│       │   ├── TransferController.kt # 송금 관련 API
│       │   ├── UserController.kt     # 사용자 관련 API
│       │   └── DeviceController.kt   # 디바이스 관련 API
│       ├── service/
│       │   ├── AccountService.kt     # 계좌 비즈니스 로직
│       │   ├── TransferService.kt    # 송금 비즈니스 로직
│       │   ├── UserService.kt        # 사용자 비즈니스 로직
│       │   ├── DeviceService.kt      # 디바이스 관리
│       │   └── BleTransferCodeService.kt # BLE 송금코드 서비스
│       ├── entity/
│       │   ├── Account.kt            # 계좌 엔티티
│       │   ├── Transfer.kt           # 송금 엔티티
│       │   ├── User.kt               # 사용자 엔티티
│       │   ├── Device.kt             # 디바이스 엔티티
│       │   └── BleTransferCode.kt    # BLE 송금코드 엔티티
│       ├── repository/
│       │   ├── AccountRepository.kt  # 계좌 데이터 접근
│       │   ├── TransferRepository.kt # 송금 데이터 접근
│       │   ├── UserRepository.kt     # 사용자 데이터 접근
│       │   ├── DeviceRepository.kt   # 디바이스 데이터 접근
│       │   └── BleTransferCodeRepository.kt # BLE 코드 데이터 접근
│       ├── dto/
│       │   ├── AccountDto.kt         # 계좌 DTO
│       │   ├── TransferDto.kt        # 송금 DTO
│       │   ├── UserDto.kt            # 사용자 DTO
│       │   ├── DeviceDto.kt          # 디바이스 DTO
│       │   └── ApiResponse.kt        # 공통 응답 DTO
│       └── config/
│           ├── Config.kt             # 기본 설정
│           ├── SecurityConfig.kt     # 보안 설정
│           └── DefaultUserConfig.kt  # 기본 사용자 설정
│
└── README.md                         # 이 파일
```

## 🚀 빠른 시작

### 1. 백엔드 서버 실행

```bash
cd backend
./gradlew bootRun
```

서버가 `http://localhost:8080/api/v1`에서 실행됩니다.

### 2. 안드로이드 앱 빌드

```bash
cd android
./gradlew assembleDebug
```

### 3. 안드로이드 앱 실행

Android Studio에서 프로젝트를 열고 실행하거나:

```bash
cd android
./gradlew installDebug
```

## 🏗️ 시스템 아키텍처

### 하이브리드 보안 모델

```
[안드로이드 앱] ──BLE 스캔/광고──> [신한은행 송금코드 교환]
       ↓                                    ↓
[백엔드 서버] ──────REST API─────> [실제 송금 처리]
       ↓                                    ↓
[신한은행 코어뱅킹] ←─────────────> [계좌이체 완료]
```

### 핵심 구성 요소

1. **안드로이드 앱** (`android/`)

   - BLE 스캔/광고를 통한 사용자 발견
   - 송금 UI/UX 제공
   - 백엔드 서버와 REST API 통신

2. **백엔드 서버** (`backend/`)
   - 사용자 인증 및 계좌 관리
   - BLE 송금코드 생성/검증
   - 송금 내역 관리

## 🔐 보안 구조

| 계층             | 보안 방식             | 보호 대상     |
| ---------------- | --------------------- | ------------- |
| **BLE 통신**     | 디바이스 ID 기반 식별 | 사용자 구분   |
| **HTTP 통신**    | REST API              | 서버 통신     |
| **백엔드 서버**  | Spring Security       | 인증/인가     |
| **데이터베이스** | JPA                   | 사용자 데이터 |

## 📱 주요 기능

### 안드로이드 앱

- **🔍 BLE 스캔**: Nordic Scanner Library 기반 주변 사용자 발견
  - 실시간 근거리 사용자 검색 및 시각화
  - ProximityVisualizationView로 거리별 사용자 표시
- **📡 BLE 광고**: 내 송금코드 브로드캐스트
  - 디바이스 정보와 송금코드 암호화 전송
- **💰 완전한 송금 플로우**:
  1.  **계좌 선택** - 송금할 계좌 선택 (AccountSelectionActivity)
  2.  **사용자 스캔** - BLE로 주변 사용자 검색 (MainActivity)
  3.  **금액 입력** - 직관적인 숫자패드로 금액 입력 (TransferAmountActivity)
  4.  **송금 확인** - 송금 정보 최종 확인 (TransferConfirmationActivity)
  5.  **결과 확인** - 송금 완료 결과 표시 (TransferResultActivity)
- **🎨 SOL 브랜드 UI**: 신한은행 SOL 페이 디자인 시스템 적용
- **🏦 계좌 관리**: 신한은행 계좌 조회 및 잔액 확인
- **🔒 보안**: 디바이스 ID 기반 사용자 식별

### 백엔드 서버

- **🔐 사용자 인증**: 기본 사용자 인증 시스템
- **📊 계좌 관리 API**:
  - 계좌 목록/상세/잔액 조회
  - 계좌 유효성 검증
- **🔄 송금 처리**:
  - BLE 송금코드 생성/검증
  - 실제 송금 처리 및 상태 관리
- **📈 내역 관리**: 송금 내역 조회 및 상태 추적
- **📱 디바이스 관리**: BLE 디바이스 등록 및 관리
- **🛡️ 보안**: Spring Security 기반 인증/인가

## 🛠️ 기술 스택

### 안드로이드 앱

- **언어**: Kotlin
- **아키텍처**: MVVM + Repository Pattern
- **UI**: Android View System + SOL 브랜드 디자인 시스템
  - CardView 기반 모던 UI
  - Gradient 헤더 및 브랜딩
  - Material Design 3.0 컴포넌트
- **BLE**: Nordic Android Scanner Library
  - 실시간 스캔 및 광고
  - Mock/Real 구현체 분리
- **네트워킹**: Retrofit + OkHttp
  - 인증/에러 인터셉터 구현
- **DI**: Hilt (Dagger)
- **시각화**: Custom ProximityVisualizationView
- **보안**: 디바이스 고유 ID 기반 식별

### 백엔드 서버

- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.2
- **보안**: Spring Security
- **데이터베이스**: MySQL
- **ORM**: JPA + Hibernate
- **API**: REST API with JSON
- **아키텍처**:
  - Controller-Service-Repository 패턴
  - DTO 기반 데이터 전송
  - 엔티티 기반 도메인 모델

## 📋 API 명세

### 사용자 관리

```
GET  /api/v1/users                # 사용자 목록 조회
POST /api/v1/users                # 사용자 생성
GET  /api/v1/users/{id}           # 사용자 상세 조회
PUT  /api/v1/users/{id}           # 사용자 정보 수정
```

### 계좌 관리

```
GET  /api/v1/accounts             # 계좌 목록 조회
POST /api/v1/accounts             # 계좌 등록
GET  /api/v1/accounts/{id}        # 계좌 상세 조회
PUT  /api/v1/accounts/{id}        # 계좌 정보 수정
GET  /api/v1/accounts/{id}/balance # 계좌 잔액 조회
```

### 송금 관리

```
POST /api/v1/transfers/validate-code # 송금코드 검증
POST /api/v1/transfers/execute       # 송금 실행
GET  /api/v1/transfers/history       # 송금 내역 조회
GET  /api/v1/transfers/{id}          # 송금 상세 조회
POST /api/v1/transfers/generate-code # 송금코드 생성
```

### 디바이스 관리

```
GET  /api/v1/devices              # 등록된 디바이스 목록
POST /api/v1/devices              # 디바이스 등록
PUT  /api/v1/devices/{id}         # 디바이스 정보 수정
DELETE /api/v1/devices/{id}       # 디바이스 삭제
```

### BLE 송금코드 관리

```
POST /api/v1/ble-codes/generate   # BLE 송금코드 생성
POST /api/v1/ble-codes/validate   # BLE 송금코드 검증
GET  /api/v1/ble-codes/{code}     # 송금코드 정보 조회
PUT  /api/v1/ble-codes/{code}     # 송금코드 상태 업데이트
```

## 🔧 개발 환경 설정

### 요구사항

- **JDK**: 17+
- **Android Studio**: Arctic Fox+
- **Kotlin**: 1.9+
- **MySQL**: 8.0+

### 환경 변수

```bash
# 백엔드 서버
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:mysql://localhost:3306/shinhan_ble
export DB_USERNAME=your-username
export DB_PASSWORD=your-password

# 안드로이드 앱 (개발 서버 URL)
# 에뮬레이터에서 로컬 백엔드 접속
export DEV_BASE_URL=http://10.0.2.2:8080/api/v1/

# 실기기에서 로컬 백엔드 접속(adb reverse 필요)
adb reverse tcp:8080 tcp:8080
export DEV_BASE_URL=http://127.0.0.1:8080/api/v1/

# 고정 IP로 직접 접속 시(테스트 폰 사용할 때)
# export DEV_BASE_URL=http://192.168.0.10:8080/api/v1/

# 일시적으로 실행 커맨드에 인라인 지정도 가능
# DEV_BASE_URL="http://127.0.0.1:8080/api/v1/" ./gradlew :app:installDebug
```

## 📈 확장 가능성

- **다른 은행 연동**: 오픈뱅킹 API 통합
- **QR 코드 지원**: BLE + QR 하이브리드 방식
- **NFC 연동**: 근거리 결제 확장
- **해외 송금**: 국제 송금 서비스
