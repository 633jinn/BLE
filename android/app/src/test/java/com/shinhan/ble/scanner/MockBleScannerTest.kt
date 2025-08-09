package com.shinhan.ble.scanner

import com.shinhan.ble.data.TestableScannedUser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BLE scanner abstraction
 * Demonstrates how to test BLE functionality without Android dependencies
 */
class BleScannerTest {

    private lateinit var testCallback: TestBleScanCallback

    @Before
    fun setUp() {
        testCallback = TestBleScanCallback()
    }

    @Test
    fun `test testable user creation`() {
        // When: Creating a testable user
        val user = TestableScannedUser.createTestUser(
            deviceName = "Test Device",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            rssi = -50
        )
        
        // Then: Should have expected properties
        assertEquals("Device name should match", "Test Device", user.deviceName)
        assertEquals("Device address should match", "AA:BB:CC:DD:EE:FF", user.deviceAddress)
        assertEquals("RSSI should match", -50, user.rssi)
        assertNotEquals("Color should be generated", 0, user.color)
    }

    @Test
    fun `test proximity calculations`() {
        // Test different proximity levels
        assertEquals("Very close proximity", 0, TestableScannedUser.getProximityLevel(-35))
        assertEquals("Close proximity", 1, TestableScannedUser.getProximityLevel(-50))
        assertEquals("Medium proximity", 2, TestableScannedUser.getProximityLevel(-70))
        assertEquals("Far proximity", 3, TestableScannedUser.getProximityLevel(-90))
    }

    @Test
    fun `test distance calculations`() {
        // Test relative distance calculations
        val closeDistance = TestableScannedUser.getRelativeDistance(-40)
        val farDistance = TestableScannedUser.getRelativeDistance(-80)
        
        assertTrue("Close distance should be smaller", closeDistance < farDistance)
        assertTrue("Distances should be in valid range", closeDistance in 0.0..1.0)
        assertTrue("Distances should be in valid range", farDistance in 0.0..1.0)
    }

    @Test
    fun `test callback interface structure`() {
        // Given: A test callback implementation
        val callback = testCallback
        
        // When: Simulating callbacks
        val testUser = TestableScannedUser.createTestUser()
        callback.onUserFound(testUser)
        callback.onScanFailed(1)
        callback.onScanStateChanged(true)
        
        // Then: Callback should track calls
        assertEquals("Should record user found", 1, callback.usersFound.size)
        assertEquals("Should record scan failure", 1, callback.scanFailures.size)
        assertEquals("Should record state changes", 1, callback.stateChanges.size)
        
        assertTrue("Should record correct state", callback.stateChanges[0])
        assertEquals("Should record correct error", 1, callback.scanFailures[0])
        assertEquals("Should record correct user", testUser, callback.usersFound[0])
    }
}

/**
 * Test implementation of BleScanCallback for unit testing
 */
class TestBleScanCallback : BleScanCallback {
    val usersFound = mutableListOf<TestableScannedUser>()
    val scanFailures = mutableListOf<Int>()
    val stateChanges = mutableListOf<Boolean>()

    fun onUserFound(user: TestableScannedUser) {
        usersFound.add(user)
    }

    override fun onUserFound(user: com.shinhan.ble.data.ScannedUser) {
        // For testing, we'll simulate with a testable user
        val testableUser = TestableScannedUser.createTestUser(
            user.deviceName, 
            user.deviceAddress, 
            user.rssi
        )
        onUserFound(testableUser)
    }

    override fun onScanFailed(errorCode: Int) {
        scanFailures.add(errorCode)
    }

    override fun onScanStateChanged(isScanning: Boolean) {
        stateChanges.add(isScanning)
    }
}