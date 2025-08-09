package com.shinhan.ble.scanner

import android.os.Handler
import android.os.Looper
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.TestableScannedUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock implementation of BleScanner for testing
 * Simulates BLE scanning behavior without requiring actual hardware
 */
@Singleton
class MockBleScanner @Inject constructor() : BleScanner {
    
    private var isCurrentlyScanning = false
    private var currentCallback: BleScanCallback? = null
    private val handler = Handler(Looper.getMainLooper())
    private var simulationRunnable: Runnable? = null
    
    // Pre-generated mock users for consistent testing
    private val testableUsers = createMockUsersWithProximity()
    private var discoveredUsers = mutableSetOf<String>() // Track discovered device addresses
    
    override fun startScan(callback: BleScanCallback): Boolean {
        if (isCurrentlyScanning) {
            return false
        }
        
        isCurrentlyScanning = true
        currentCallback = callback
        discoveredUsers.clear()
        
        callback.onScanStateChanged(true)
        
        // Simulate discovering devices over time
        startDeviceDiscoverySimulation()
        
        return true
    }
    
    override fun stopScan() {
        if (!isCurrentlyScanning) {
            return
        }
        
        isCurrentlyScanning = false
        simulationRunnable?.let { handler.removeCallbacks(it) }
        currentCallback?.onScanStateChanged(false)
        currentCallback = null
    }
    
    override fun isScanning(): Boolean {
        return isCurrentlyScanning
    }
    
    override fun isBleReady(): Boolean {
        // Mock always returns true for testing
        return true
    }
    
    /**
     * Simulate device discovery over time
     */
    private fun startDeviceDiscoverySimulation() {
        simulationRunnable = object : Runnable {
            override fun run() {
                if (!isCurrentlyScanning) return
                
                // Randomly discover a new device or update existing one
                val shouldDiscoverNew = discoveredUsers.size < testableUsers.size && Random.nextBoolean()
                
                if (shouldDiscoverNew) {
                    // Discover a new device
                    val undiscoveredUsers = testableUsers.filter { it.deviceAddress !in discoveredUsers }
                    if (undiscoveredUsers.isNotEmpty()) {
                        val newUser = undiscoveredUsers.random()
                        discoveredUsers.add(newUser.deviceAddress)
                        currentCallback?.onUserFound(convertToScannedUser(newUser))
                    }
                } else {
                    // Update an existing device with new RSSI
                    val discoveredAddresses = discoveredUsers.toList()
                    if (discoveredAddresses.isNotEmpty()) {
                        val addressToUpdate = discoveredAddresses.random()
                        val originalUser = testableUsers.find { it.deviceAddress == addressToUpdate }
                        originalUser?.let { user ->
                            // Create updated user with slightly different RSSI
                            val updatedUser = TestableScannedUser(
                                deviceAddress = user.deviceAddress,
                                deviceName = user.deviceName,
                                rssi = user.rssi + Random.nextInt(-5, 6), // Simulate RSSI fluctuation
                                color = user.color
                            )
                            currentCallback?.onUserFound(convertToScannedUser(updatedUser))
                        }
                    }
                }
                
                // Schedule next discovery event
                if (isCurrentlyScanning) {
                    handler.postDelayed(this, Random.nextLong(1000, 3000)) // 1-3 seconds
                }
            }
        }
        
        // Start the first discovery immediately
        handler.post(simulationRunnable!!)
    }
    
    /**
     * Create mock users with specific RSSI values for proximity testing
     */
    private fun createMockUsersWithProximity(): List<TestableScannedUser> {
        return listOf(
            TestableScannedUser.createTestUser("Very Close Device", "AA:BB:CC:DD:EE:11", -35),    // Very close
            TestableScannedUser.createTestUser("Close Device", "AA:BB:CC:DD:EE:22", -50),         // Close  
            TestableScannedUser.createTestUser("Medium Device", "AA:BB:CC:DD:EE:33", -70),        // Medium
            TestableScannedUser.createTestUser("Far Device", "AA:BB:CC:DD:EE:44", -85)            // Far
        )
    }
    
    /**
     * Convert TestableScannedUser to ScannedUser for callback compatibility
     */
    private fun convertToScannedUser(testableUser: TestableScannedUser): ScannedUser {
        throw UnsupportedOperationException(
            "MockBleScanner is deprecated. Switch to RealBleScanner for production use."
        )
    }
    
    /**
     * For testing: manually trigger discovery of a specific user
     */
    fun simulateUserDiscovery(user: ScannedUser) {
        if (isCurrentlyScanning) {
            currentCallback?.onUserFound(user)
        }
    }
    
    /**
     * For testing: simulate scan failure
     */
    fun simulateScanFailure(errorCode: Int = 1) {
        if (isCurrentlyScanning) {
            currentCallback?.onScanFailed(errorCode)
        }
    }
}