package com.shinhan.ble.scanner

import com.shinhan.ble.data.ScannedUser

/**
 * Abstraction for BLE scanning functionality
 * This interface allows for easy testing with mock implementations
 */
interface BleScanner {
    /**
     * Start scanning for BLE devices
     * @param callback Callback to receive scan results
     * @return true if scanning started successfully, false otherwise
     */
    fun startScan(callback: BleScanCallback): Boolean
    
    /**
     * Stop scanning for BLE devices
     */
    fun stopScan()
    
    /**
     * Check if currently scanning
     * @return true if scanning is active
     */
    fun isScanning(): Boolean
    
    /**
     * Check if BLE is available and enabled
     * @return true if BLE is ready to use
     */
    fun isBleReady(): Boolean
}

/**
 * Callback interface for BLE scan results
 */
interface BleScanCallback {
    /**
     * Called when a new device is discovered or an existing device is updated
     * @param user The scanned user data
     */
    fun onUserFound(user: ScannedUser)
    
    /**
     * Called when scanning fails
     * @param errorCode Error code indicating the failure reason
     */
    fun onScanFailed(errorCode: Int)
    
    /**
     * Called when scan state changes
     * @param isScanning Current scanning state
     */
    fun onScanStateChanged(isScanning: Boolean)
}