package com.shinhan.ble.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for testable data structures
 * Demonstrates how to test with mock data without Android dependencies
 */
class TestableScannedUserTest {

    @Test
    fun `test create testable user with default values`() {
        // When: Creating a testable user with defaults
        val user = TestableScannedUser.createTestUser()
        
        // Then: Should have valid properties
        assertEquals("Device name should match default", "Test Device", user.deviceName)
        assertEquals("Device address should match default", "AA:BB:CC:DD:EE:FF", user.deviceAddress)
        assertEquals("RSSI should match default", -50, user.rssi)
        assertNotEquals("Color should be set", 0, user.color)
    }

    @Test
    fun `test create testable user with specific parameters`() {
        // Given: Specific parameters
        val expectedName = "Alice's iPhone"
        val expectedAddress = "11:22:33:44:55:66"
        val expectedRssi = -65
        
        // When: Creating testable user with parameters
        val user = TestableScannedUser.createTestUser(
            deviceName = expectedName,
            deviceAddress = expectedAddress,
            rssi = expectedRssi
        )
        
        // Then: Should match specified parameters
        assertEquals("Device name should match", expectedName, user.deviceName)
        assertEquals("Device address should match", expectedAddress, user.deviceAddress)
        assertEquals("RSSI should match", expectedRssi, user.rssi)
    }

    @Test
    fun `test consistent color generation`() {
        // Given: Same device address
        val address = "11:22:33:44:55:66"
        
        // When: Creating multiple users with same address
        val user1 = TestableScannedUser.createTestUser(deviceAddress = address)
        val user2 = TestableScannedUser.createTestUser(deviceAddress = address)
        
        // Then: Should have same color
        assertEquals("Users with same address should have same color", user1.color, user2.color)
    }

    @Test
    fun `test different addresses have different colors`() {
        // Given: Different device addresses
        val address1 = "11:22:33:44:55:66"
        val address2 = "AA:BB:CC:DD:EE:FF"
        
        // When: Creating users with different addresses
        val user1 = TestableScannedUser.createTestUser(deviceAddress = address1)
        val user2 = TestableScannedUser.createTestUser(deviceAddress = address2)
        
        // Then: Should have different colors (most likely)
        assertNotEquals("Users with different addresses should have different colors", user1.color, user2.color)
    }

    @Test
    fun `test proximity level calculation`() {
        // Test proximity level calculation
        assertEquals("Very close RSSI should be level 0", 0, TestableScannedUser.getProximityLevel(-35))
        assertEquals("Close RSSI should be level 1", 1, TestableScannedUser.getProximityLevel(-50))
        assertEquals("Medium RSSI should be level 2", 2, TestableScannedUser.getProximityLevel(-70))
        assertEquals("Far RSSI should be level 3", 3, TestableScannedUser.getProximityLevel(-85))
        
        // Boundary testing
        assertEquals("Boundary at -40 should be level 0", 0, TestableScannedUser.getProximityLevel(-40))
        assertEquals("Boundary at -60 should be level 1", 1, TestableScannedUser.getProximityLevel(-60))
        assertEquals("Boundary at -80 should be level 2", 2, TestableScannedUser.getProximityLevel(-80))
    }

    @Test
    fun `test relative distance calculation`() {
        // Test relative distance calculation
        val veryCloseDistance = TestableScannedUser.getRelativeDistance(-35)
        val farDistance = TestableScannedUser.getRelativeDistance(-85)
        
        assertTrue("Very close should have smaller distance", veryCloseDistance < farDistance)
        assertTrue("Distance should be between 0.0 and 1.0", veryCloseDistance in 0.0..1.0)
        assertTrue("Distance should be between 0.0 and 1.0", farDistance in 0.0..1.0)
        
        // Test edge cases
        val strongestSignal = TestableScannedUser.getRelativeDistance(-30)
        val weakestSignal = TestableScannedUser.getRelativeDistance(-100)
        
        assertTrue("Strongest signal should have distance close to 0", strongestSignal < 0.2f)
        assertTrue("Weakest signal should have distance close to 1", weakestSignal > 0.8f)
    }

    @Test
    fun `test data class properties`() {
        // Given: A testable user
        val user = TestableScannedUser.createTestUser(
            deviceName = "Test Device",
            deviceAddress = "11:22:33:44:55:66",
            rssi = -45
        )
        
        // When: Accessing properties
        val name = user.deviceName
        val address = user.deviceAddress
        val rssi = user.rssi
        val color = user.color
        
        // Then: Properties should be accessible and correct
        assertEquals("Device name property", "Test Device", name)
        assertEquals("Device address property", "11:22:33:44:55:66", address)
        assertEquals("RSSI property", -45, rssi)
        assertNotEquals("Color property should be set", 0, color)
    }

    @Test
    fun `test user equality`() {
        // Given: Two users with same data
        val user1 = TestableScannedUser("AA:BB:CC:DD:EE:FF", "Test Device", -50, 12345)
        val user2 = TestableScannedUser("AA:BB:CC:DD:EE:FF", "Test Device", -50, 12345)
        val user3 = TestableScannedUser("11:22:33:44:55:66", "Test Device", -50, 12345)
        
        // Then: Users with same data should be equal
        assertEquals("Users with same data should be equal", user1, user2)
        assertNotEquals("Users with different addresses should not be equal", user1, user3)
    }
}