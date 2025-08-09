# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android BLE (Bluetooth Low Energy) scanner application built with Kotlin. The project uses Nordic Semiconductor's Android-Scanner-Compat-Library for robust BLE scanning functionality and is designed for devices running Android API 33+.

## Architecture

### Core Components
- **MainActivity** (`app/src/main/java/com/shinhan/ble/MainActivity.kt`): Main activity that handles BLE scanning, permissions, and UI management. Uses Nordic Scanner Compat library for enhanced BLE scanning.
- **DeviceAdapter** (`app/src/main/java/com/shinhan/ble/DeviceAdapter.kt`): RecyclerView adapter for displaying scanned BLE devices with name, MAC address, and RSSI values.

### Key Dependencies
- **Nordic BLE Libraries**: 
  - `no.nordicsemi.android.support.v18:scanner:1.6.0` - Enhanced BLE scanning
  - `no.nordicsemi.android:ble:2.6.1` - Complete BLE functionality
- **Architecture Components**: 
  - Lifecycle ViewModel (2.7.0)
  - Navigation Fragment (2.7.6)
  - Room Database (2.6.1) with Kotlin coroutines support
- **Dependency Injection**: Hilt (2.48)
- **Security**: AndroidX Security Crypto (1.1.0-alpha06)

### Package Structure
- Base package: `com.shinhan.ble`
- All source files use Nordic Scanner Compat library (`no.nordicsemi.android.support.v18.scanner`)
- Minimum SDK: 33, Target SDK: 36

## Development Commands

### Build Commands
```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew testDebugUnitTest
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Check for lint issues
./gradlew lintDebug
```

## Project Configuration

- **Gradle Version**: 8.11.1
- **Kotlin Version**: 2.0.21
- **Compile SDK**: 36
- **Min SDK**: 33
- **Target SDK**: 36
- **Java Version**: 11

## BLE Implementation Notes

- Uses Nordic Scanner Compat library for improved compatibility across Android versions
- Requires BLUETOOTH_SCAN, BLUETOOTH_CONNECT, and ACCESS_FINE_LOCATION permissions
- Implements proper permission handling and Bluetooth activation flow
- Scanner settings configured for low-latency mode with 5-second batch reporting
- Device list updates in real-time with duplicate detection by MAC address

## Important Development Considerations

- All BLE operations require proper permission checks before execution
- MainActivity handles ActivityResult for Bluetooth activation
- DeviceAdapter requires BLUETOOTH_CONNECT permission for device name access
- The project is set up for additional features like Room database and Hilt dependency injection
- Security crypto library is available for secure data handling