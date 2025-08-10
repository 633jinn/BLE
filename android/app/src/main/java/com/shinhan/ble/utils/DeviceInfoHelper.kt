package com.shinhan.ble.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.shinhan.ble.BuildConfig
import com.shinhan.ble.data.network.dto.DeviceRegistrationDto
import java.util.*

/**
 * 디바이스 정보 수집 도우미 클래스
 */
object DeviceInfoHelper {

    /**
     * 디바이스 등록 정보 생성
     */
    fun createDeviceRegistrationDto(
        context: Context,
        userId: String? = null
    ): DeviceRegistrationDto {
        return DeviceRegistrationDto(
            deviceName = getDeviceName(),
            deviceModel = getDeviceModel(),
            osVersion = getOsVersion(),
            appVersion = getAppVersion(context),
            deviceId = getDeviceId(context),
            userId = userId ?: generateUniqueUserId(context)
        )
    }

    /**
     * 디바이스 이름 가져오기
     * Android 설정에서 설정한 디바이스 이름 또는 모델명
     */
    private fun getDeviceName(): String {
        return Build.MODEL
    }

    /**
     * 블루투스 로컬 이름 가져오기 (설정된 기기명 → 없으면 모델명)
     */
    fun getBluetoothLocalName(context: Context): String {
        return try {
            val name = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
                ?: Settings.Secure.getString(context.contentResolver, "bluetooth_name")
                ?: Build.MODEL
            name.ifBlank { Build.MODEL }
        } catch (e: Exception) {
            Build.MODEL
        }
    }

    /**
     * 디바이스 모델 가져오기
     */
    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }

    /**
     * OS 버전 가져오기
     */
    private fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    /**
     * 앱 버전 가져오기
     */
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "${packageInfo.versionName} ($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            BuildConfig.VERSION_NAME
        }
    }

    /**
     * 디바이스 고유 ID 생성
     * Android ID 또는 UUID 기반 고유 식별자
     */
    private fun getDeviceId(context: Context): String {
        return try {
            // Android ID 사용 (디바이스 재설정 시 변경됨)
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            if (androidId != null && androidId != "9774d56d682e549c") {
                androidId
            } else {
                // Android ID가 없거나 에뮬레이터인 경우 UUID 생성
                generateUniqueDeviceId(context)
            }
        } catch (e: Exception) {
            generateUniqueDeviceId(context)
        }
    }

    /**
     * 고유 디바이스 ID 생성 및 저장
     */
    private fun generateUniqueDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }

        return deviceId
    }

    /**
     * 고유 사용자 ID 생성 및 저장
     * 디바이스 ID를 기반으로 일관된 사용자 ID 생성
     */
    private fun generateUniqueUserId(context: Context): String {
        val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)

        if (userId == null) {
            // 디바이스 ID를 기반으로 일관된 사용자 ID 생성
            val deviceId = getDeviceId(context)
            userId = UUID.nameUUIDFromBytes("USER_$deviceId".toByteArray()).toString()
            prefs.edit().putString("user_id", userId).apply()
        }

        return userId
    }

    /**
     * 디바이스 정보 요약 생성
     */
    fun getDeviceSummary(context: Context): String {
        return buildString {
            append("Device: ${getDeviceName()}\n")
            append("Model: ${getDeviceModel()}\n")
            append("OS: ${getOsVersion()}\n")
            append("App: ${getAppVersion(context)}\n")
            append("ID: ${getDeviceId(context)}")
        }
    }
}