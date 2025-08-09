package com.shinhan.ble.data

import androidx.core.graphics.toColorInt
import kotlin.math.abs

/**
 * Testable version of ScannedUser that doesn't depend on Android ScanResult
 * Can be used in unit tests without Android runtime
 */
data class TestableScannedUser(
    val deviceAddress: String,
    val deviceName: String,
    val rssi: Int,
    val color: Int
) {
    companion object {
        private val colorPalette = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
        )
        
        fun createTestUser(
            deviceName: String = "Test Device",
            deviceAddress: String = "AA:BB:CC:DD:EE:FF",
            rssi: Int = -50
        ): TestableScannedUser {
            val colorIndex = abs(deviceAddress.hashCode()) % colorPalette.size
            val colorString = colorPalette[colorIndex]
            // For testing, we'll use a simple color value instead of Android's color parsing
            val color = colorString.hashCode()
            
            return TestableScannedUser(
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                rssi = rssi,
                color = color
            )
        }
        
        // Utility methods that don't depend on Android
        fun getProximityLevel(rssi: Int): Int {
            return when {
                rssi >= -40 -> 0  // Very close
                rssi >= -60 -> 1  // Close
                rssi >= -80 -> 2  // Medium
                else -> 3         // Far
            }
        }
        
        fun getRelativeDistance(rssi: Int): Float {
            val normalizedRssi = (rssi + 100).coerceIn(0, 60)
            return (60 - normalizedRssi) / 60f
        }
    }
}