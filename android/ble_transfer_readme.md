# BLE Transfer System

> **BLE를 이용한 안드로이드 계좌 송금 시스템**  
> Nordic Android Scanner Library를 활용한 근거리 P2P 송금 애플리케이션

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📱 프로젝트 개요

BLE Transfer는 Bluetooth Low Energy(BLE) 기술을 활용하여 근거리에 있는 사용자 간 직접적인 계좌 송금을 가능하게 하는 혁신적인 Android 애플리케이션입니다. 복잡한 계좌번호 입력이나 QR코드 스캔 없이, 단순히 BLE 스캔을 통해 근처 사용자를 찾아 즉시 송금할 수 있습니다.

### ✨ 주요 특징

- **🔍 간편한 스캔**: 한 번의 터치로 근처 사용자 자동 감지
- **⚡ 빠른 송금**: 3단계만으로 송금 완료 (스캔 → 계좌 선택 → 송금)
- **🔒 안전한 거래**: AES 암호화 및 잔액 검증으로 안전한 송금
- **📶 오프라인 지원**: 인터넷 연결 없이도 BLE만으로 송금 가능
- **🎨 직관적 UI**: 누구나 쉽게 사용할 수 있는 사용자 친화적 인터페이스

## 🚀 시작하기

### 시스템 요구사항

- **Android**: 13.0 (API 33) 이상
- **BLE**: Bluetooth 4.0 이상 지원 디바이스
- **권한**: 위치, 블루투스 권한 필요
- **RAM**: 최소 2GB 이상 권장

### 설치 방법

1. **저장소 클론**

   ```bash
   git clone https://github.com/your-username/BLE.git
   cd BLE
   ```

2. **Android Studio에서 프로젝트 열기**

   ```
   File → Open → BLE 폴더 선택
   ```

3. **종속성 동기화**

   ```bash
   ./gradlew build
   ```

4. **앱 실행**
   - Android 디바이스 연결
   - Run 버튼 클릭 또는 `./gradlew installDebug`

## 📋 사용 방법

### 현재 기능 (v0.1.0)

1. **🔍 BLE 스캔**

   - 메인 화면에서 "스캔 시작" 버튼 터치
   - Nordic Scanner Library로 주변 BLE 디바이스 감지
   - 실시간 디바이스 목록 업데이트

2. **📱 디바이스 정보 표시**

   - 디바이스 이름 (또는 "Unknown Device")
   - MAC 주소
   - RSSI 값 (신호 강도)

3. **⚙️ 권한 관리**
   - Android 13+ 블루투스 권한 자동 요청
   - 위치 권한 요청 (BLE 스캔용)
   - 블루투스 활성화 요청

### 향후 송금 플로우 (v1.1.0+)

1. **👤 사용자 선택**

   - 스캔된 사용자 목록에서 송금 대상 선택
   - 사용자명과 디바이스 정보 확인

2. **💳 계좌 선택**

   - 본인의 등록된 계좌 목록에서 출금 계좌 선택
   - 각 계좌의 현재 잔액 확인

3. **💰 금액 입력**

   - 숫자 키패드로 송금할 금액 입력
   - 잔액 충분 여부 실시간 확인

4. **✅ 송금 완료**
   - "송금하기" 버튼으로 최종 실행
   - 완료 후 자동으로 메인 화면 이동

### 스크린샷

```
[현재 구현 - v0.1.0]
┌─────────────────┐
│ 🔵 스캔 시작    │
│ 🛑 스캔 중지    │
│                 │
│ 📱 Galaxy S23   │
│    12:34:56:78  │
│    RSSI: -45dBm │
│                 │
│ 📱 iPhone 14    │
│    AA:BB:CC:DD  │
│    RSSI: -52dBm │
└─────────────────┘

[향후 구현 - v1.1.0+]
[계좌 선택]         [송금 화면]
┌─────────────┐     ┌─────────────┐
│ 💳 국민은행 │ →   │ 💰 50,000원 │
│ 💳 신한은행 │     │ [1][2][3]   │
│ 💳 우리은행 │     │ [4][5][6]   │
└─────────────┘     │ [송금하기]  │
                    └─────────────┘
```

## 🏗️ 프로젝트 구조

```
BLE/
├── 📁 app/                           # 안드로이드 애플리케이션
│   ├── build.gradle.kts              # 앱 빌드 설정
│   ├── proguard-rules.pro            # ProGuard 설정
│   └── src/
│       ├── androidTest/
│       │   └── java/com/shinhan/ble/ # 안드로이드 테스트
│       │       └── ExampleInstrumentedTest.kt
│       ├── main/
│       │   ├── AndroidManifest.xml   # 앱 매니페스트
│       │   ├── java/com/shinhan/ble/ # 메인 소스 코드
│       │   │   ├── MainActivity.kt   # 메인 액티비티 (BLE 스캔)
│       │   │   └── DeviceAdapter.kt  # 디바이스 목록 어댑터
│       │   └── res/                  # 리소스 파일들
│       │       ├── layout/           # 레이아웃 파일
│       │       ├── values/           # 문자열, 색상 등
│       │       └── drawable/         # 이미지 리소스
│       └── test/
│           └── java/com/shinhan/ble/ # 단위 테스트
│               └── ExampleUnitTest.kt
├── 📄 build.gradle.kts               # 프로젝트 빌드 설정
├── 📄 settings.gradle.kts            # 프로젝트 설정
├── 📁 gradle/                        # Gradle 설정
│   ├── libs.versions.toml            # 의존성 버전 관리
│   └── wrapper/                      # Gradle Wrapper
└── 📄 ble_transfer_readme.md         # 프로젝트 문서
```

## 🛠️ 기술 스택

### Android 앱

- **언어**: Kotlin 1.9+
- **아키텍처**: Android View System (현재 구현)
- **UI**: Material Design Components
- **BLE**: Nordic Android Scanner Library v1.6.0
- **최소 SDK**: API 33 (Android 13)
- **타겟 SDK**: API 36
- **테스트**: JUnit, Espresso (기본 템플릿)

### 향후 계획 (백엔드)

- **언어**: Kotlin
- **프레임워크**: Spring Boot
- **데이터베이스**: PostgreSQL
- **보안**: JWT, AES 암호화
- **API**: RESTful API

### 현재 사용 중인 라이브러리

```kotlin
dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Nordic BLE Scanner Library
    implementation(libs.scanner)  // Gradle 버전 카탈로그에서 관리
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### 향후 추가 예정 라이브러리

```kotlin
// Architecture Components (v2.0에서 추가 예정)
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")

// Database (Room) - 계좌 정보 저장용
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Dependency Injection (Hilt)
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")

// Security (암호화)
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

## 🔧 개발 환경 설정

### 1. 개발 도구 설치

```bash
# Android Studio 최신 버전 설치
# JDK 17 이상 설치
# Android SDK 34 이상 설치
```

### 2. 프로젝트 설정

```bash
# 환경 변수 설정
export ANDROID_HOME=/path/to/android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Gradle 래퍼 권한 설정 (macOS/Linux)
chmod +x gradlew
```

### 3. 빌드 및 실행

```bash
# 디버그 빌드
./gradlew assembleDebug

# 릴리즈 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test
./gradlew connectedAndroidTest
```

## 🧪 테스트

### 단위 테스트

```bash
# 모든 단위 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :app:testDebugUnitTest
```

### UI 테스트

```bash
# 기기에서 UI 테스트 실행
./gradlew connectedAndroidTest

# 특정 테스트 클래스 실행
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.bletransfer.MainActivityTest
```
## 🔐 보안

### 데이터 보안

- **계좌 정보**: AES-256 암호화 저장
- **송금 데이터**: 디지털 서명 적용
- **통신**: BLE 페어링 + 암호화

### 권한 관리

```xml
<!-- BLE 권한들 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- BLE 기능 선언 -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```
