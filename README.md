# SOL BLE Transfer System

> **신한은행 쏠(SOL) 내 주변 송금 서비스**  
> 안드로이드 앱 + 백엔드 서버로 구성된 완전한 BLE 기반 P2P 송금 시스템

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)

## 📁 프로젝트 구조

```
SOL-BLE-Transfer/
├── android/                    # 안드로이드 앱
│   ├── app/
│   │   ├── src/main/java/com/shinhan/ble/
│   │   │   ├── MainActivity.kt
│   │   │   ├── advertiser/         # BLE Advertiser
│   │   │   ├── data/              # 데이터 모델 & Repository
│   │   │   ├── di/                # Hilt 의존성 주입
│   │   │   └── scanner/           # BLE Scanner
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── backend/                    # Spring Boot 백엔드 서버
│   ├── src/main/kotlin/com/shinhan/ble/
│   │   ├── BleBackendApplication.kt
│   │   ├── controller/            # REST API 컨트롤러
│   │   ├── service/              # 비즈니스 로직
│   │   ├── entity/               # JPA 엔티티
│   │   ├── repository/           # 데이터 접근 계층
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── config/               # 설정 클래스
│   │   └── security/             # 보안 설정
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── shared/                     # 공통 모델
│   └── models/
│       └── ShinhanApiModels.kt  # 공통 데이터 모델
│
├── docs/                       # 문서
│   ├── api-spec.md             # API 명세서
│   ├── architecture.md         # 아키텍처 문서
│   └── deployment.md           # 배포 가이드
│
└── README.md                   # 이 파일
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

### 3. API 테스트

H2 콘솔: http://localhost:8080/api/v1/h2-console

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
   - 신한은행 코어뱅킹 연동 (Mock)
   - 송금 내역 관리

3. **공통 모델** (`shared/`)
   - 앱과 서버 간 데이터 모델 통일
   - API 스펙 정의

## 🔐 보안 구조

| 계층 | 보안 방식 | 보호 대상 |
|------|-----------|--------------|
| **BLE 통신** | AES-128 암호화 | 송금코드 교환 |
| **HTTP 통신** | TLS 1.3 + JWT | API 통신 |
| **백엔드 서버** | Spring Security | 인증/인가 |
| **데이터베이스** | JPA + 암호화 | 사용자 데이터 |

## 📱 주요 기능

### 안드로이드 앱
- **🔍 BLE 스캔**: Nordic Scanner Library 기반 주변 사용자 발견
- **📡 BLE 광고**: 내 송금코드 브로드캐스트
- **💰 송금 플로우**: 스캔 → 선택 → 금액 입력 → 확인
- **🏦 계좌 관리**: 신한은행 계좌 조회 및 잔액 확인

### 백엔드 서버
- **🔐 JWT 인증**: 안전한 사용자 인증
- **📊 계좌 API**: 계좌 목록/상세/잔액 조회
- **🔄 송금 처리**: 송금코드 검증 및 실제 이체
- **📈 내역 관리**: 송금 내역 조회 및 상태 추적

## 🛠️ 기술 스택

### 안드로이드 앱
- **언어**: Kotlin
- **아키텍처**: MVVM + Repository Pattern
- **UI**: Android View System + Material Design
- **BLE**: Nordic Android Scanner Library
- **네트워킹**: Retrofit + OkHttp
- **DI**: Hilt
- **로컬 DB**: Room

### 백엔드 서버
- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.2
- **보안**: Spring Security + JWT
- **데이터베이스**: PostgreSQL (운영) / H2 (개발)
- **ORM**: JPA + Hibernate
- **API**: REST + Swagger

## 📋 API 명세

주요 엔드포인트:

```
POST /auth/login          # 로그인
GET  /accounts           # 계좌 목록 조회
POST /transfer/validate-code  # 송금코드 검증
POST /transfer/execute   # 송금 실행
GET  /transfer/history   # 송금 내역
POST /transfer/generate-code  # 내 송금코드 생성
```

자세한 API 명세: [docs/api-spec.md](docs/api-spec.md)

## 🔧 개발 환경 설정

### 요구사항
- **JDK**: 17+
- **Android Studio**: Arctic Fox+
- **Kotlin**: 1.9+
- **PostgreSQL**: 13+ (운영 시)

### 환경 변수
```bash
# 백엔드 서버
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/shinhan_ble
export JWT_SECRET=your-jwt-secret-key
```

## 📈 확장 가능성

- **다른 은행 연동**: 오픈뱅킹 API 통합
- **QR 코드 지원**: BLE + QR 하이브리드 방식
- **NFC 연동**: 근거리 결제 확장
- **해외 송금**: 국제 송금 서비스

## 📄 라이선스

이 프로젝트는 신한은행의 내부 프로젝트로 개발되었습니다.

## 👥 기여자

- **개발**: 신한은행 디지털혁신부
- **기술 지원**: Claude AI Assistant