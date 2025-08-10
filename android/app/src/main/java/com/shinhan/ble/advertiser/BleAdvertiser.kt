package com.shinhan.ble.advertiser

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.shinhan.ble.data.ShinhanBLEData
import com.shinhan.ble.utils.DeviceInfoHelper
import java.util.*

/**
 * 신한은행 BLE Advertiser
 * 신한은행 송금코드를 BLE로 브로드캐스트하여 다른 신한은행 고객이 발견할 수 있도록 함
 */
class BleAdvertiser(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private var currentAdvertiseCallback: AdvertiseCallback? = null
    
    // 신한은행 전용 Service UUID (실제로는 신한은행에서 등록한 UUID 사용)
    private val shinhanServiceUuid = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    
    // 현재 브로드캐스트 중인 신한은행 데이터
    private var currentShinhanData: ShinhanBLEData? = null
    
    // 광고 콜백 인터페이스
    interface AdvertiseListener {
        fun onAdvertiseStarted()
        fun onAdvertiseFailed(errorCode: Int)
        fun onAdvertiseStopped()
    }
    
    private var listener: AdvertiseListener? = null
    
    /**
     * Advertise 콜백 설정
     */
    fun setAdvertiseListener(listener: AdvertiseListener) {
        this.listener = listener
    }
    
    /**
     * BLE Advertise 시작
     */
    @SuppressLint("MissingPermission")
    fun startAdvertising(shinhanData: ShinhanBLEData) {
        // 권한 확인
        if (!hasAdvertisePermissions()) {
            val missingPermissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add("BLUETOOTH_ADVERTISE")
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add("BLUETOOTH_CONNECT")
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add("BLUETOOTH_ADMIN")
                }
            }
            Log.e(TAG, "Missing required permissions for advertising: ${missingPermissions.joinToString(", ")}")
            listener?.onAdvertiseFailed(AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
            return
        }
        
        // 블루투스 및 BLE Advertise 지원 확인
        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            listener?.onAdvertiseFailed(AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
            return
        }
        
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            Log.e(TAG, "BLE advertising is not supported on this device")
            listener?.onAdvertiseFailed(AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
            return
        }
        
        // 이미 광고 중이면 먼저 중지
        if (isAdvertising) {
            stopAdvertising()
        }
        
        try {
            advertiser = bluetoothAdapter.bluetoothLeAdvertiser
            if (advertiser == null) {
                Log.e(TAG, "BluetoothLeAdvertiser is null - advertising not supported")
                listener?.onAdvertiseFailed(AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
                return
            }
            
            currentShinhanData = shinhanData
            // 로컬 이름 보장: 비어있거나 기본값이면 설정에서 가져온 이름으로 세팅
            ensureBluetoothLocalName()
            Log.d(TAG, "Starting advertising with transfer code: ${shinhanData.transferCode}")
            
            // 광고 설정
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED) // 저전력/발견성 균형
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // 발열/전력 고려
                .setConnectable(false) // 연결 불가 (스캔만 목적)
                .setTimeout(0) // 무제한 광고 (수동으로 중지할 때까지)
                .build()
            
            // 서비스 데이터 크기 체크
            val serviceData = createServiceData(shinhanData)
            val customerNameData = shinhanData.customerName.toByteArray()
            
            Log.d(TAG, "Service data size: ${serviceData.size} bytes")
            Log.d(TAG, "Customer name data size: ${customerNameData.size} bytes")
            
            // 광고 데이터 구성 (크기 최적화)
            val advertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(shinhanServiceUuid))
                // 일부 단말 호환성: serviceData가 큰 경우 UUID만 싣고 scanResponse에 나머지 배분
                .build()
            
            // 스캔 응답 데이터 (추가 정보) - 디바이스 이름은 포함하지 않음 (크기 초과 방지)
            val scanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceData(ParcelUuid(shinhanServiceUuid), serviceData)
                .build()
            
            // 광고 데이터 상세 로그
            Log.d("BLE_ADVERTISE", "=== STARTING BLE ADVERTISEMENT ===")
            Log.d("BLE_ADVERTISE", "Service UUID: $shinhanServiceUuid")
            Log.d("BLE_ADVERTISE", "Transfer Code: ${shinhanData.transferCode}")
            Log.d("BLE_ADVERTISE", "Customer Name: ${shinhanData.customerName}")
            Log.d("BLE_ADVERTISE", "Service Data: ${String(serviceData)} (${serviceData.size} bytes)")
            Log.d("BLE_ADVERTISE", "Advertise Mode: ${settings.mode}")
            Log.d("BLE_ADVERTISE", "TX Power Level: ${settings.txPowerLevel}")
            Log.d("BLE_ADVERTISE", "Timeout: ${settings.timeout}ms")
            Log.d("BLE_ADVERTISE", "Connectable: ${settings.isConnectable}")
            Log.d("BLE_ADVERTISE", "======================================")
            
            // 광고 콜백 생성
            currentAdvertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)
                    isAdvertising = true
                    Log.d("BLE_ADVERTISE", "✅ ADVERTISEMENT STARTED SUCCESSFULLY")
                    Log.d("BLE_ADVERTISE", "Settings in effect: $settingsInEffect")
                    Log.d("BLE_ADVERTISE", "Broadcasting transfer code: ${shinhanData.transferCode}")
                    Log.d("BLE_ADVERTISE", "Broadcasting customer name: ${shinhanData.customerName}")
                    Log.d("BLE_ADVERTISE", "🎯 NOW ADVERTISING - Other devices should discover us")
                    listener?.onAdvertiseStarted()
                }
                
                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                    isAdvertising = false
                    Log.e("BLE_ADVERTISE", "❌ ADVERTISEMENT FAILED")
                    Log.e("BLE_ADVERTISE", "Error code: $errorCode")
                    Log.e("BLE_ADVERTISE", "Error description: ${getErrorString(errorCode)}")
                    listener?.onAdvertiseFailed(errorCode)
                }
            }
            
            // 광고 시작
            advertiser?.startAdvertising(settings, advertiseData, scanResponse, currentAdvertiseCallback)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start advertising", e)
            listener?.onAdvertiseFailed(AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR)
        }
    }
    
    /**
     * BLE Advertise 중지
     */
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (isAdvertising && advertiser != null && currentAdvertiseCallback != null) {
            try {
                advertiser?.stopAdvertising(currentAdvertiseCallback)
                isAdvertising = false
                currentShinhanData = null
                Log.d(TAG, "BLE advertising stopped")
                listener?.onAdvertiseStopped()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop advertising", e)
            }
        }
        currentAdvertiseCallback = null
    }
    
    /**
     * 현재 광고 중인지 확인
     */
    fun isAdvertising(): Boolean = isAdvertising
    
    /**
     * 현재 브로드캐스트 중인 신한은행 데이터
     */
    fun getCurrentShinhanData(): ShinhanBLEData? = currentShinhanData
    
    /**
     * 신한은행 데이터를 BLE 서비스 데이터로 변환
     * 형식: [송금코드][구분자][고객명]
     */
    private fun createServiceData(shinhanData: ShinhanBLEData): ByteArray {
        // Android 레거시 광고(31 bytes) 한도 내에서 Service Data(AD type 0x16, 16-bit UUID 가정) 안전화
        // 가정한 계산: 31 - (length 1 + type 1 + uuid 2) = 최대 payload 27 bytes
        val transferCode = shinhanData.transferCode
        val customerName = shinhanData.customerName
        val delimiter = "|"

        val maxPayloadBytes = 27 // 31 - 1(len) - 1(type) - 2(uuid for 16-bit)
        val codeBytes = transferCode.toByteArray()
        val delimiterBytes = delimiter.toByteArray()
        val originalNameBytes = customerName.toByteArray()
        // 남는 바이트 만큼만 이름을 잘라서 포함 (UTF-8 멀티바이트 경계 보존)
        val remainingForName = (maxPayloadBytes - codeBytes.size - delimiterBytes.size).coerceAtLeast(0)
        val truncatedNameBytes = if (remainingForName <= 0) {
            ByteArray(0)
        } else {
            val nameBuilder = java.io.ByteArrayOutputStream()
            for (ch in customerName) {
                val b = ch.toString().toByteArray()
                if (nameBuilder.size() + b.size > remainingForName) break
                nameBuilder.write(b)
            }
            nameBuilder.toByteArray()
        }

        // 최종 결합
        val combinedBytes = ByteArray(codeBytes.size + delimiterBytes.size + truncatedNameBytes.size)
        System.arraycopy(codeBytes, 0, combinedBytes, 0, codeBytes.size)
        System.arraycopy(delimiterBytes, 0, combinedBytes, codeBytes.size, delimiterBytes.size)
        System.arraycopy(truncatedNameBytes, 0, combinedBytes, codeBytes.size + delimiterBytes.size, truncatedNameBytes.size)

        Log.d(TAG, "Creating service data: ${String(combinedBytes)} (bytes=${combinedBytes.size})")
        if (truncatedNameBytes.size < originalNameBytes.size) {
            Log.w(TAG, "Customer name truncated to fit advertising limit (orig=${originalNameBytes.size}, used=${truncatedNameBytes.size})")
        }
        return combinedBytes
    }
    
    /**
     * BLE Advertise 권한 확인
     */
    private fun hasAdvertisePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 에서는 BLUETOOTH_ADVERTISE와 BLUETOOTH_CONNECT 권한 모두 필요
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 미만에서는 BLUETOOTH_ADMIN 권한으로 충분
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 광고 에러 코드를 문자열로 변환
     */
    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED"
            AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE"
            AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
            AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR"
            AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
            else -> "UNKNOWN_ERROR_$errorCode"
        }
    }
    
    companion object {
        private const val TAG = "BleAdvertiser"
    }

    @SuppressLint("MissingPermission")
    private fun ensureBluetoothLocalName() {
        try {
            val current = bluetoothAdapter.name
            if (current.isNullOrBlank() || current.startsWith("Device") || current.startsWith("디바이스")) {
                val desired = DeviceInfoHelper.getBluetoothLocalName(context)
                // 이름 설정은 일부 기기에서 제한될 수 있음. 권한 필요(ADB, 시스템 정책 등).
                bluetoothAdapter.name = desired
                Log.d(TAG, "Bluetooth local name set to: $desired (was: $current)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to ensure bluetooth local name: ${e.message}")
        }
    }
}