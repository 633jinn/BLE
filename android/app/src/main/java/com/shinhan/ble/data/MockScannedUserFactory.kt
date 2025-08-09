package com.shinhan.ble.data

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.core.graphics.toColorInt
import no.nordicsemi.android.support.v18.scanner.ScanResult as NordicScanResult
import kotlin.math.abs
import kotlin.random.Random

/**
 * Factory for creating mock ScannedUser objects for testing
 */
object MockScannedUserFactory {
    
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
    
    private val mockDeviceNames = listOf(
        "Alice's iPhone", "Bob's Galaxy", "Carol's MacBook", 
        "David's AirPods", "Eve's Watch", "Frank's Tablet",
        "Grace's Headphones", "Henry's Laptop", "Ivy's Phone"
    )
    
    /**
     * Create a mock ScannedUser with predefined data
     */
    fun createMockUser(
        deviceName: String = mockDeviceNames.random(),
        deviceAddress: String = generateMockAddress(),
        rssi: Int = Random.nextInt(-90, -30)
    ): ScannedUser {
        val colorIndex = abs(deviceAddress.hashCode()) % colorPalette.size
        val color = colorPalette[colorIndex]
        
        // Mock implementation throws error - use TestableScannedUser for testing instead
        throw UnsupportedOperationException(
            "MockScannedUserFactory is deprecated. Use TestableScannedUser.createTestUser() for testing instead."
        )
    }
    
    /**
     * Create a list of mock users for testing
     */
    fun createMockUserList(count: Int = 5): List<ScannedUser> {
        return (1..count).map {
            createMockUser(
                deviceName = mockDeviceNames.getOrElse(it - 1) { "Device $it" },
                rssi = Random.nextInt(-90, -30)
            )
        }
    }
    
    /**
     * Create mock users with specific RSSI values for proximity testing
     */
    fun createMockUsersWithProximity(): List<ScannedUser> {
        return listOf(
            createMockUser("Very Close Device", generateMockAddress(), -35),    // Very close
            createMockUser("Close Device", generateMockAddress(), -50),         // Close  
            createMockUser("Medium Device", generateMockAddress(), -70),        // Medium
            createMockUser("Far Device", generateMockAddress(), -85)            // Far
        )
    }
    
    private fun generateMockAddress(): String {
        return (1..6).joinToString(":") { 
            Random.nextInt(0, 256).toString(16).padStart(2, '0').uppercase()
        }
    }
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun createMockScanResult(
        deviceName: String, 
        deviceAddress: String, 
        rssi: Int
    ): NordicScanResult {
        // For now, create a placeholder mock that won't be used in main app logic
        // This is only needed for ScannedUser compatibility
        // To be used in actual logic later - Real ScanResult creation when using real scanner
        
        // Return null for now - this factory is mainly for testing the abstracted version
        throw UnsupportedOperationException(
            "MockScannedUserFactory.createMockScanResult should not be called in main app. " +
            "Use TestableScannedUser.createTestUser() for testing or switch to real BLE implementation."
        )
    }
}