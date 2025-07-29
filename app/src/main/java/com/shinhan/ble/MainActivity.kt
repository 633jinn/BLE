package com.shinhan.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter as NordicScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult as NordicScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings as NordicScanSettings
import java.util.*

/**
 * MainActivity - Nordic Semiconductor의 BLE Scanner Compat 라이브러리를 사용한 BLE 스캐너
 * 
 * 주요 기능:
 * - 주변 BLE 디바이스 스캔
 * - 실시간 디바이스 목록 표시
 * - 권한 자동 요청
 * - 블루투스 활성화 요청
 */
class MainActivity : AppCompatActivity() {
    
    // 블루투스 관련 컴포넌트들
    private lateinit var bluetoothAdapter: BluetoothAdapter  // 블루투스 어댑터
    private lateinit var scanner: BluetoothLeScannerCompat   // Nordic Scanner Compat 인스턴스
    
    // UI 컴포넌트들
    private lateinit var startScanButton: Button    // 스캔 시작 버튼
    private lateinit var stopScanButton: Button     // 스캔 중지 버튼
    private lateinit var statusText: TextView       // 상태 표시 텍스트
    private lateinit var deviceList: RecyclerView   // 디바이스 목록 리사이클러뷰
    private lateinit var deviceAdapter: DeviceAdapter // 디바이스 어댑터
    
    // 블루투스 활성화 결과 처리를 위한 ActivityResultLauncher
    private lateinit var enableBtLauncher: ActivityResultLauncher<Intent>

    // 스캔된 디바이스 목록 (Nordic Scanner의 ScanResult 타입 사용)
    private val devices = mutableListOf<NordicScanResult>()
    private var isScanning = false  // 현재 스캔 중인지 여부
    
    /**
     * BLE 스캔 콜백 - Nordic Scanner Compat 라이브러리 사용
     * 
     * onScanResult: 새로운 디바이스 발견 시 호출
     * onScanFailed: 스캔 실패 시 호출
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: NordicScanResult
        ) {
            super.onScanResult(callbackType, result)
            // UI 업데이트는 메인 스레드에서 실행
            runOnUiThread {
                addOrUpdateDevice(result)
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            runOnUiThread {
                statusText.text = getString(R.string.scan_failed, errorCode)
                stopScanning()
            }
        }
    }
    
    companion object {
        private const val REQUEST_ENABLE_BT = 1      // 블루투스 활성화 요청 코드
        private const val REQUEST_PERMISSIONS = 2    // 권한 요청 코드
        
        // BLE 스캔에 필요한 권한들
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,      // Android 12+ BLE 스캔 권한
            Manifest.permission.BLUETOOTH_CONNECT,   // Android 12+ BLE 연결 권한
            Manifest.permission.ACCESS_FINE_LOCATION // 위치 권한 (BLE 스캔에 필요)
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // 엣지-투-엣지 디스플레이 지원
        setContentView(R.layout.activity_main)
        
        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 블루투스 활성화 결과 처리를 위한 ActivityResultLauncher 등록
        enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())  { result ->
            if (result.resultCode == RESULT_OK) {
                // 블루투스가 활성화됨 - 스캔 시작
                startScanning()
            } else {
                // 블루투스 활성화 거부됨
                Toast.makeText(this, getString(R.string.bluetooth_required), Toast.LENGTH_SHORT).show()
            }
        }

        // 초기화 작업들
        initializeViews()      // UI 컴포넌트 초기화
        initializeBluetooth()  // 블루투스 관련 초기화
        setupRecyclerView()    // 리사이클러뷰 설정
    }
    
    /**
     * UI 컴포넌트들을 초기화하고 클릭 리스너 설정
     */
    private fun initializeViews() {
        startScanButton = findViewById(R.id.startScanButton)
        stopScanButton = findViewById(R.id.stopScanButton)
        statusText = findViewById(R.id.statusText)
        deviceList = findViewById(R.id.deviceList)
        
        // 버튼 클릭 리스너 설정
        startScanButton.setOnClickListener { startScanning() }
        stopScanButton.setOnClickListener { stopScanning() }
    }
    
    /**
     * 블루투스 어댑터와 Nordic Scanner 초기화
     */
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        scanner = BluetoothLeScannerCompat.getScanner()  // Nordic Scanner Compat 인스턴스 생성
    }
    
    /**
     * RecyclerView 설정 - 디바이스 목록 표시용
     */
    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(devices)
        deviceList.layoutManager = LinearLayoutManager(this)
        deviceList.adapter = deviceAdapter
    }
    
    /**
     * BLE 스캔 시작
     * 
     * 1. 권한 확인
     * 2. 블루투스 활성화 확인
     * 3. Nordic Scanner Compat를 사용한 스캔 시작
     */
//    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun startScanning() {
        // 권한 확인
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        
        // 블루투스 활성화 확인
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
            return
        }
        
        // 디바이스 목록 초기화
        devices.clear()
        deviceAdapter.notifyDataSetChanged()
        
        // Nordic Scanner Compat 스캔 설정
        val settings = NordicScanSettings.Builder()
            .setLegacy(false)  // 새로운 스캔 API 사용 (Android 5.0+)
            .setScanMode(NordicScanSettings.SCAN_MODE_LOW_LATENCY)  // 최대 성능 모드
            .setReportDelay(5000)  // 5초마다 배치 결과 전송
            .build()
        
        // 스캔 필터 설정 (현재는 모든 디바이스 스캔)
        val filters = ArrayList<NordicScanFilter>()
        // 특정 서비스 UUID로 필터링하려면 아래 주석을 해제하세요
        // filters.add(NordicScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("YOUR-UUID-HERE"))).build())
        
        try {
            // Nordic Scanner Compat로 스캔 시작
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true
            startScanButton.isEnabled = false
            stopScanButton.isEnabled = true
            statusText.text = getString(R.string.status_scanning)
            Toast.makeText(this, getString(R.string.scanning_started), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.failed_to_start_scan, e.message), Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * BLE 스캔 중지
     */
    private fun stopScanning() {
        if (isScanning) {
            try {
                scanner.stopScan(scanCallback)
                isScanning = false
                startScanButton.isEnabled = true
                stopScanButton.isEnabled = false
                statusText.text = getString(R.string.status_stopped, devices.size)
                Toast.makeText(this, getString(R.string.scanning_stopped), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.failed_to_stop_scan, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 스캔된 디바이스를 목록에 추가하거나 업데이트
     * 
     * @param result 스캔 결과 (Nordic Scanner의 ScanResult)
     */
    private fun addOrUpdateDevice(result: NordicScanResult) {
        // 기존 디바이스인지 MAC 주소로 확인
        val existingIndex = devices.indexOfFirst { it.device.address == result.device.address }
        
        if (existingIndex >= 0) {
            // 기존 디바이스 업데이트 (RSSI 등 정보 갱신)
            devices[existingIndex] = result
            deviceAdapter.notifyItemChanged(existingIndex)
        } else {
            // 새로운 디바이스 추가
            devices.add(result)
            deviceAdapter.notifyItemInserted(devices.size - 1)
        }
    }
    
    /**
     * 필요한 권한들이 모두 허용되었는지 확인
     * 
     * @return 모든 권한이 허용되면 true, 아니면 false
     */
    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 필요한 권한들을 요청
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
    }
    
    /**
     * 권한 요청 결과 처리
     * 
     * @param requestCode 요청 코드
     * @param permissions 요청한 권한들
     * @param grantResults 권한 허용 결과들
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // 모든 권한이 허용됨 - 스캔 시작
                startScanning()
            } else {
                // 권한이 거부됨
                Toast.makeText(this, getString(R.string.permissions_required), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Activity 결과 처리 (블루투스 활성화 요청 결과)
     * 
     * @param requestCode 요청 코드
     * @param resultCode 결과 코드
     * @param data 결과 데이터
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // 블루투스 활성화됨 - 스캔 시작
                startScanning()
            } else {
                // 블루투스 활성화 거부됨
                Toast.makeText(this, getString(R.string.bluetooth_must_be_enabled), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Activity 종료 시 스캔 중지
     */
    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
    }
}