package com.shinhan.ble.data

import android.Manifest
import android.graphics.Color
import android.os.Parcelable
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.support.v18.scanner.ScanResult as NordicScanResult
import kotlin.random.Random
import androidx.core.graphics.toColorInt
import kotlin.math.abs
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import java.util.*

@Parcelize
data class ScannedUser(
    val deviceAddress: String,
    val deviceName: String,
    val rssi: Int,
    val color: Int,
    val shinhanData: ShinhanBLEData? = null, // 신한은행 데이터 (신한은행 사용자인 경우만)
    @IgnoredOnParcel val scanResult: NordicScanResult? = null
) : Parcelable {
    companion object {
        // 신한은행 전용 Service UUID (실제로는 신한은행에서 등록한 UUID 사용)
        private val SHINHAN_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")

        private val colorPalette = listOf(
            "#FF6B6B".toColorInt(), // Red
            "#4ECDC4".toColorInt(), // Teal
            "#45B7D1".toColorInt(), // Blue
            "#96CEB4".toColorInt(), // Green
            "#FFEAA7".toColorInt(), // Yellow
            "#DDA0DD".toColorInt(), // Plum
            "#98D8C8".toColorInt(), // Mint
            "#F7DC6F".toColorInt(), // Light Yellow
            "#BB8FCE".toColorInt(), // Light Purple
            "#85C1E9".toColorInt()  // Light Blue
        )

        // 신한은행 사용자용 색상 (파란색 계열)
        private val shinhanColorPalette = listOf(
            "#0066CC".toColorInt(), // Shinhan Blue
            "#3385FF".toColorInt(), // Light Shinhan Blue
            "#66A3FF".toColorInt(), // Lighter Blue
            "#0052A3".toColorInt()  // Dark Shinhan Blue
        )

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun fromScanResult(scanResult: NordicScanResult): ScannedUser {
            Log.d("ScannedUser", "🥹name은 ${scanResult.device.name}")
            Log.d("ScannedUser", "🥹name은 ${scanResult.scanRecord?.deviceName}")
            val deviceName = scanResult.scanRecord?.deviceName
                ?: "디바이스 ${scanResult.device.address.takeLast(5).replace(":", "")}"
            val deviceAddress = scanResult.device.address
            val rssi = scanResult.rssi
            Log.d("ScannedUser", "🥹name은 ${deviceName}")

            // 신한은행 데이터 추출 시도
            val shinhanData = extractShinhanData(scanResult)
            val isShinhanUser = shinhanData != null

            // 신한은행 사용자는 파란색, 일반 사용자는 다양한 색상
            val color = if (isShinhanUser) {
                val colorIndex = abs(deviceAddress.hashCode()) % shinhanColorPalette.size
                shinhanColorPalette[colorIndex]
            } else {
                val colorIndex = abs(deviceAddress.hashCode()) % colorPalette.size
                colorPalette[colorIndex]
            }
            return ScannedUser(
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                rssi = rssi,
                color = color,
                shinhanData = shinhanData,
                scanResult = scanResult
            )
        }

        /**
         * 스캔 결과에서 신한은행 데이터 추출
         */
        private fun extractShinhanData(scanResult: NordicScanResult): ShinhanBLEData? {
            try {
                val scanRecord = scanResult.scanRecord
                if (scanRecord == null) {
                    Log.d("ScannedUser", "No scan record found for device: ${scanResult.device.address}")
                    return null
                }

                Log.d("ScannedUser", "Extracting Shinhan data from device: ${scanResult.device.address}")
                Log.d("ScannedUser", "Available service UUIDs: ${scanRecord.serviceUuids?.joinToString { it.toString() }}")

                // Service Data에서 신한은행 데이터 찾기
                val serviceData = scanRecord.getServiceData(ParcelUuid(SHINHAN_SERVICE_UUID))

                if (serviceData != null) {
                    val rawData = String(serviceData)
                    Log.d("ScannedUser", "Found service data: $rawData (${serviceData.size} bytes)")

                    // 데이터 파싱: "송금코드|고객명" 형식
                    val parts = rawData.split("|")
                    if (parts.size == 2) {
                        val transferCode = parts[0]
                        val customerName = parts[1]

                        Log.d("ScannedUser", "Parsed - Transfer code: $transferCode, Customer: $customerName")

                        // 신한은행 송금코드 인지 확인
                        if (ShinhanBLEData.isValidShinhanData(transferCode)) {
                            Log.d("ScannedUser", "Valid Shinhan transfer code found: $transferCode")

                            return ShinhanBLEData(
                                transferCode = transferCode,
                                customerName = customerName
                            )
                        } else {
                            Log.d("ScannedUser", "Invalid Shinhan data format: $transferCode")
                        }
                    } else {
                        Log.d("ScannedUser", "Invalid data format, expected 'code|name': $rawData")
                    }
                } else {
                    Log.d("ScannedUser", "No service data found for Shinhan UUID: $SHINHAN_SERVICE_UUID")
                    // 모든 service data 로깅
                    val allServiceData = scanRecord.serviceData
                    if (allServiceData != null && allServiceData.isNotEmpty()) {
                        Log.d("ScannedUser", "Available service data:")
                        allServiceData.forEach { (uuid, data) ->
                            Log.d("ScannedUser", "  UUID: $uuid, Data: ${String(data)} (${data.size} bytes)")
                        }
                    } else {
                        Log.d("ScannedUser", "No service data found at all")
                    }
                }

                return null
            } catch (e: Exception) {
                Log.e("ScannedUser", "Error extracting Shinhan data from ${scanResult.device.address}", e)
                return null
            }
        }

        // Convert RSSI to proximity level (0-3, where 0 is closest)
        fun getProximityLevel(rssi: Int): Int {
            return when {
                rssi >= -40 -> 0  // Very close
                rssi >= -60 -> 1  // Close
                rssi >= -80 -> 2  // Medium
                else -> 3         // Far
            }
        }

        // Get relative distance for positioning (0.0 to 1.0, where 0.0 is center)
        fun getRelativeDistance(rssi: Int): Float {
            val normalizedRssi = (rssi + 100).coerceIn(0, 60) // Normalize -100 to -40 dBm to 0-60
            return (60 - normalizedRssi) / 60f // Invert so stronger signal = closer to center
        }
    }

    /**
     * 신한은행 사용자인지 확인
     */
    fun isShinhanUser(): Boolean = shinhanData != null

    /**
     * 송금 코드 가져오기
     */
    val transferCode: String?
        get() = shinhanData?.transferCode

}