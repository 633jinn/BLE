package com.shinhan.ble.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.shinhan.ble.data.ScannedUser
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter as NordicScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult as NordicScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings as NordicScanSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real BLE scanner implementation using Nordic Scanner Compat library
 */
@Singleton
class RealBleScanner @Inject constructor(
    @ApplicationContext private val context: Context
) : BleScanner {
    
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    private val scanner: BluetoothLeScannerCompat by lazy {
        BluetoothLeScannerCompat.getScanner()
    }
    
    private var isCurrentlyScanning = false
    private var currentCallback: BleScanCallback? = null
    
    // Real Nordic scanner callback
    @SuppressLint("MissingPermission")
    private val nordicScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: NordicScanResult) {
            super.onScanResult(callbackType, result)
            
            if (hasBluetoothPermissions()) {
                try {
                    val scannedUser = ScannedUser.fromScanResult(result)
                    currentCallback?.onUserFound(scannedUser)
                } catch (e: Exception) {
                    // Handle any errors in conversion
                    currentCallback?.onScanFailed(-1)
                }
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isCurrentlyScanning = false
            currentCallback?.onScanFailed(errorCode)
            currentCallback?.onScanStateChanged(false)
        }
    }
    
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun startScan(callback: BleScanCallback): Boolean {
        if (isCurrentlyScanning || !isBleReady()) {
            return false
        }
        
        if (!hasBluetoothPermissions()) {
            callback.onScanFailed(-2) // Permission error
            return false
        }
        
        currentCallback = callback
        
        // Nordic scanner settings
        val settings = NordicScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(NordicScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(5000)
            .build()
        
        val filters = ArrayList<NordicScanFilter>()
        // TODO: Add specific service UUID filters if needed
        
        try {
            // Start real scanning
            scanner.startScan(filters, settings, nordicScanCallback)
            isCurrentlyScanning = true
            callback.onScanStateChanged(true)
            return true
        } catch (e: Exception) {
            callback.onScanFailed(-3) // Scan start failed
            return false
        }
    }
    
    override fun stopScan() {
        if (!isCurrentlyScanning) {
            return
        }
        
        try {
            // Stop real scanning
            scanner.stopScan(nordicScanCallback)
        } catch (e: Exception) {
            // Ignore errors when stopping
        }
        
        isCurrentlyScanning = false
        currentCallback?.onScanStateChanged(false)
        currentCallback = null
    }
    
    override fun isScanning(): Boolean {
        return isCurrentlyScanning
    }
    
    override fun isBleReady(): Boolean {
        // Check if BLE is available and enabled
        return try {
            bluetoothAdapter.isEnabled &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        } catch (e: Exception) {
            false
        }
    }
    
    // Permission checking logic
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}