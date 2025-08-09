package com.shinhan.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.ShinhanBLEData
import com.shinhan.ble.data.database.BleTransferRepository
import com.shinhan.ble.data.repository.ShinhanApiRepository
import com.shinhan.ble.scanner.BleScanner
import com.shinhan.ble.scanner.BleScanCallback
import com.shinhan.ble.advertiser.BleAdvertiser
import com.shinhan.ble.utils.DeviceInfoHelper
import com.shinhan.ble.utils.FirstLaunchHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
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
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    // Hilt 의존성 주입
    @Inject
    lateinit var repository: BleTransferRepository
    
    @Inject
    lateinit var apiRepository: ShinhanApiRepository
    
    // Real Bluetooth components
    private lateinit var bluetoothAdapter: BluetoothAdapter  // 블루투스 어댑터
    private lateinit var scanner: BluetoothLeScannerCompat   // Nordic Scanner Compat 인스턴스
    private lateinit var advertiser: BleAdvertiser           // BLE Advertiser
    private lateinit var bottomSheet: LinearLayout           // 바텀시트
    
    // 스캔 상태 추적
    private var isScanning = false
    private var isAdvertising = false
    private var scanTimeoutHandler: android.os.Handler? = null
    private var scanTimeoutRunnable: Runnable? = null
    
    // UI 컴포넌트들
    private lateinit var scanButton: Button                             // 스캔 버튼 (시작/중지 토글)
    private lateinit var settingsButton: Button                         // 설정 버튼
    private lateinit var statusText: TextView                           // 상태 표시 텍스트
    private lateinit var proximityView: ProximityVisualizationView      // 근접도 시각화 뷰
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>    // 바텀 시트 컨트롤러
    private lateinit var usersRecyclerView: RecyclerView               // 사용자 목록 리사이클러뷰
    private lateinit var scannedUserAdapter: ScannedUserAdapter        // 스캔된 사용자 어댑터
    
    // 블루투스 활성화 결과 처리를 위한 ActivityResultLauncher
    private lateinit var enableBtLauncher: ActivityResultLauncher<Intent>
    
    // 권한 요청 결과 처리를 위한 ActivityResultLauncher
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // 스캔된 사용자 목록
    private val scannedUsers = mutableListOf<ScannedUser>()
    
    /**
     * BLE 스캔 콜백 - Nordic Scanner 콜백
     */
    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: NordicScanResult
        ) {
            super.onScanResult(callbackType, result)
            
            // 디버깅을 위한 상세 로그
            Log.d("BLE_SCAN", "=== DEVICE FOUND ===")
            Log.d("BLE_SCAN", "Address: ${result.device.address}")
            Log.d("BLE_SCAN", "RSSI: ${result.rssi}")
            Log.d("BLE_SCAN", "Name: ${result.device.name}")
            Log.d("BLE_SCAN", "Callback Type: $callbackType")
            
            val scanRecord = result.scanRecord
            var isShinhanDevice = false
            
            if (scanRecord != null) {
                Log.d("BLE_SCAN", "Scan record available: ${scanRecord.bytes?.size} bytes")
                
                // 모든 서비스 UUID 로그
                val serviceUuids = scanRecord.serviceUuids
                if (serviceUuids != null && serviceUuids.isNotEmpty()) {
                    Log.d("BLE_SCAN", "Service UUIDs found: ${serviceUuids.map { it.uuid.toString() }}")
                } else {
                    Log.d("BLE_SCAN", "No service UUIDs found")
                }
                
                // 모든 서비스 데이터 로그
                val allServiceData = scanRecord.serviceData
                if (allServiceData != null && allServiceData.isNotEmpty()) {
                    Log.d("BLE_SCAN", "Service data found:")
                    allServiceData.forEach { (uuid, data) ->
                        Log.d("BLE_SCAN", "  UUID: $uuid, Data: ${String(data)} (${data.size} bytes)")
                    }
                } else {
                    Log.d("BLE_SCAN", "No service data found")
                }
                
                // 신한은행 서비스 확인
                val shinhanServiceUuid = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
                val serviceData = scanRecord.getServiceData(ParcelUuid(shinhanServiceUuid))
                
                if (serviceData != null) {
                    Log.d("BLE_SCAN", "✅ SHINHAN SERVICE DATA FOUND: ${String(serviceData)}")
                    isShinhanDevice = true
                } else {
                    // 일부 단말은 service data가 scanResponse로만 실릴 수 있음 -> UUID만 있어도 수락
                    val hasServiceUuid = scanRecord.serviceUuids?.any { it.uuid == shinhanServiceUuid } == true
                    if (hasServiceUuid) {
                        Log.d("BLE_SCAN", "✅ SHINHAN SERVICE UUID FOUND (no data)")
                        isShinhanDevice = true
                    } else {
                        Log.d("BLE_SCAN", "❌ NO SHINHAN SERVICE - SKIPPING")
                    }
                }
            } else {
                Log.d("BLE_SCAN", "❌ NO SCAN RECORD")
            }
            
            Log.d("BLE_SCAN", "Is Shinhan Device: $isShinhanDevice")
            Log.d("BLE_SCAN", "===================")
            
            // 신한은행 서비스 데이터가 있는 디바이스만 UI에 추가
            if (isShinhanDevice && hasBluetoothPermissions()) {
                Log.d("BLE_SCAN", "🎯 ADDING SHINHAN DEVICE TO UI: ${result.device.address}")
                runOnUiThread {
                    try {
                        addOrUpdateUser(result)
                    } catch (e: Exception) {
                        Log.e("BLE_SCAN", "Error in runOnUiThread addOrUpdateUser: ${e.message}", e)
                    }
                }
            } else if (!isShinhanDevice) {
                Log.d("BLE_SCAN", "🚫 IGNORING NON-SHINHAN DEVICE: ${result.device.address}")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val errorMessage = when (errorCode) {
                1 -> "스캔 실패: 이미 시작됨"
                2 -> "스캔 실패: 애플리케이션 등록 실패"
                3 -> "스캔 실패: 기능 미지원"
                4 -> "스캔 실패: 내부 오류"
                else -> "스캔 실패: 알 수 없는 오류 ($errorCode)"
            }
            Log.e("MainActivity", "Scan failed with error code: $errorCode")
            runOnUiThread {
                statusText.text = errorMessage
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                stopScanning()
            }
        }
    }
    
    
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1      // 블루투스 활성화 요청 코드
        private const val REQUEST_PERMISSIONS = 2    // 권한 요청 코드
        private const val SCAN_TIMEOUT_MS = 30000L   // 30 seconds scan timeout
        
        // BLE 스캔에 필요한 권한들 - Android 버전에 따라 다르게 설정
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,      // Android 12+ BLE 스캔 권한
                Manifest.permission.BLUETOOTH_CONNECT,   // Android 12+ BLE 연결 권한
                Manifest.permission.BLUETOOTH_ADVERTISE, // Android 12+ BLE 광고 권한
                Manifest.permission.ACCESS_FINE_LOCATION // 위치 권한 (BLE 스캔에 필요)
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, // 위치 권한 (BLE 스캔에 필요)
                Manifest.permission.ACCESS_COARSE_LOCATION, // 위치 권한 (Android 6.0+)
                Manifest.permission.BLUETOOTH_ADMIN       // Android 12 미만 BLE 광고 권한
            )
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_main)
        
        // Enable immersive mode after content view is set
        enableImmersiveMode()
        
        // Handle system bars for immersive mode
        setupSystemBars()
        
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
        
        // 권한 요청 결과 처리를 위한 ActivityResultLauncher 등록
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // 모든 권한이 허용됨 - 스캔 시작
                startScanning()
            } else {
                // 일부 권한이 거부됨 - 사용자에게 설명
                showPermissionRationaleDialog()
            }
        }

        // 초기화 작업들
        initializeViews()       // UI 컴포넌트 초기화
        setupBottomSheet()      // 바텀 시트 설정
        
        initializeBluetooth()   // 블루투스 관련 초기화
        
        // 첫 실행 시 디바이스 등록
        handleFirstLaunch()
    }
    
    /**
     * Immersive Mode 활성화
     */
    private fun enableImmersiveMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 (API 30) 이상
                window.insetsController?.let { controller ->
                    controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Android 10 이하
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
            
            // Status bar와 navigation bar를 투명하게 설정
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to enable immersive mode: ${e.message}")
            // Fallback to basic fullscreen mode
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }
    }
    
    /**
     * 시스템 바 처리 설정
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // 상태바와 네비게이션 바 영역까지 컨텐츠가 확장되도록 패딩 조정
            v.setPadding(
                systemBars.left,
                0, // 상태바 영역까지 확장
                systemBars.right,
                0  // 네비게이션 바 영역까지 확장
            )
            insets
        }
        
        // Android R 이상에서 시스템 바 자동 숨김 처리는 onWindowFocusChanged에서 처리
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 포커스를 다시 얻었을 때 Immersive Mode 재적용
            try {
                enableImmersiveMode()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to re-enable immersive mode: ${e.message}")
            }
        }
    }
    
    /**
     * UI 컴포넌트들을 초기화하고 클릭 리스너 설정
     */
    private fun initializeViews() {
        scanButton = findViewById(R.id.scanButton)
        settingsButton = findViewById(R.id.settingsButton)
        statusText = findViewById(R.id.statusText)
        proximityView = findViewById(R.id.proximityView)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        
        // 버튼 클릭 리스너 설정
        scanButton.setOnClickListener { toggleScanning() }
        settingsButton.setOnClickListener { openSettings() }
    }

    /**
     * 설정 화면 열기 (대표 계좌 선택)
     */
    private fun openSettings() {
        val intent = Intent(this, AccountAdditionActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 블루투스 어댑터와 Nordic Scanner 초기화
     */
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        scanner = BluetoothLeScannerCompat.getScanner()  // Nordic Scanner Compat 인스턴스 생성
        advertiser = BleAdvertiser(this, bluetoothAdapter) // BLE Advertiser 인스턴스 생성
        
        // Advertiser 리스너 설정
        advertiser.setAdvertiseListener(object : BleAdvertiser.AdvertiseListener {
            override fun onAdvertiseStarted() {
                runOnUiThread {
                    isAdvertising = true
                    updateUI()
                    Toast.makeText(this@MainActivity, "신한은행 송금코드 브로드캐스트 시작", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onAdvertiseFailed(errorCode: Int) {
                runOnUiThread {
                    isAdvertising = false
                    updateUI()
                    val errorMessage = when (errorCode) {
                        1 -> "브로드캐스트 실패: 이미 시작됨"
                        2 -> "브로드캐스트 실패: 데이터가 너무 큼"
                        3 -> "브로드캐스트 실패: 기능 미지원 (권한 확인 필요)"
                        4 -> "브로드캐스트 실패: 내부 오류"
                        5 -> "브로드캐스트 실패: 광고 개수 초과"
                        else -> "브로드캐스트 실패: 알 수 없는 오류 ($errorCode)"
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Advertising failed with error code: $errorCode")
                }
            }
            
            override fun onAdvertiseStopped() {
                runOnUiThread {
                    isAdvertising = false
                    updateUI()
                    Toast.makeText(this@MainActivity, "브로드캐스트 중지", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    
    /**
     * 바텀 시트 설정
     */
    private fun setupBottomSheet() {
        bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet_users)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        
        // Force bottom sheet to be visible after layout is complete
        bottomSheet.post {
            bottomSheetBehavior.peekHeight = 80
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.isDraggable = true
            Log.d("MainActivity", "Bottom sheet setup - peekHeight: ${bottomSheetBehavior.peekHeight}")
            Log.d("MainActivity", "Bottom sheet setup - state: ${bottomSheetBehavior.state}")
            Log.d("MainActivity", "Bottom sheet setup - isDraggable: ${bottomSheetBehavior.isDraggable}")
        }
        
        // 바텀시트 상태 변경 리스너 추가
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val stateName = when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> "COLLAPSED"
                    BottomSheetBehavior.STATE_EXPANDED -> "EXPANDED" 
                    BottomSheetBehavior.STATE_HIDDEN -> "HIDDEN"
                    BottomSheetBehavior.STATE_DRAGGING -> "DRAGGING"
                    BottomSheetBehavior.STATE_SETTLING -> "SETTLING"
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> "HALF_EXPANDED"
                    else -> "UNKNOWN($newState)"
                }
                Log.d("MainActivity", "Bottom sheet state changed to: $stateName")
                
                // Prevent bottom sheet from being hidden
//                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
//                    Log.d("MainActivity", "Bottom sheet tried to hide, forcing back to COLLAPSED")
//                    bottomSheet.post {
//                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중에는 로그를 찍지 않음 (너무 많이 호출됨)
            }
        })
        
        // 사용자 어댑터 설정 - 빈 리스트로 초기화
        scannedUserAdapter = ScannedUserAdapter(mutableListOf()) { user ->
            onUserSelected(user)
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = scannedUserAdapter
    }
    
    /**
     * 스캔 시작/중지 토글
     */
    private fun toggleScanning() {
        if (isScanning) {
            stopScanning()
        } else {
            startScanning()
        }
    }
    
    // 광고 시작 중인지 추적하는 플래그
    private var isStartingAdvertising = false
    
    /**
     * 신한은행 BLE 광고 시작
     */
    private fun startAdvertising() {
        Log.d("MainActivity", "startAdvertising() 호출됨")
        Log.d("MainActivity", "상태 체크 - isStartingAdvertising: $isStartingAdvertising, isAdvertising: $isAdvertising, advertiser.isAdvertising(): ${advertiser.isAdvertising()}")
        
        // 이미 광고 시작 중이거나 광고 중이면 무시
        if (isStartingAdvertising || isAdvertising || advertiser.isAdvertising()) {
            Log.w("MainActivity", "BLE 광고가 이미 시작되었거나 시작 중입니다.")
            Toast.makeText(this, "BLE 광고가 이미 시작됨", Toast.LENGTH_SHORT).show()
            return
        }
        
        isStartingAdvertising = true
        Log.d("MainActivity", "BLE 광고 시작 프로세스 시작, isStartingAdvertising = true")
        
        // 백엔드 API를 통해 송금코드 생성 후 BLE 광고 시작
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "1. 사용자 정보 가져오기 시작")
                // 1. 사용자 정보 가져오기
                val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(this@MainActivity)
                val userId = deviceRegistrationDto.userId ?: run {
                    Log.e("MainActivity", "사용자 ID가 null입니다")
                    showError("사용자 정보를 가져올 수 없습니다.")
                    isStartingAdvertising = false // 실패 시 플래그 리셋
                    return@launch
                }
                Log.d("MainActivity", "사용자 ID 획득: $userId")
                
                Log.d("MainActivity", "2. 대표 계좌 우선 조회 시작")
                // 2. 대표 계좌 우선 조회, 없으면 전체 계좌에서 첫 번째 사용
                val primaryResult = repository.getPrimaryAccount(userId)
                val primaryAccount = primaryResult.getOrNull()
                val resolvedAccount = if (primaryAccount != null) {
                    Log.d("MainActivity", "대표 계좌 사용: ${primaryAccount.accountId}")
                    primaryAccount
                } else {
                    Log.d("MainActivity", "대표 계좌 없음 → 전체 계좌 조회")
                    val accountsResult = repository.getUserAccounts(userId)
                    val accounts = accountsResult.getOrNull()
                    if (accounts.isNullOrEmpty()) {
                        Log.e("MainActivity", "활성 계좌가 없습니다")
                        showError("송금 가능한 계좌가 없습니다.")
                        isStartingAdvertising = false
                        return@launch
                    }
                    accounts.first()
                }
                Log.d("MainActivity", "송금 계좌 선택: ${resolvedAccount.accountId}")
                    
                    // 3. 백엔드 API로 송금코드 생성
                    Log.d("MainActivity", "3. 송금코드 생성 요청 시작")
                    val codeGenerationDto = com.shinhan.ble.data.network.dto.BleTransferCodeGenerationDto(
                        userId = userId,
                        accountId = resolvedAccount.accountId
                    )
                    
                    val codeResult = repository.generateTransferCode(codeGenerationDto)
                    codeResult.onSuccess { bleTransferCodeDto ->
                        Log.d("MainActivity", "송금코드 생성 성공: ${bleTransferCodeDto.transferCode}")
                        // 4. 백엔드에서 받은 데이터로 BLE 광고 시작
                        val shinhanData = ShinhanBLEData.createFromBackendResponse(bleTransferCodeDto)
                        Log.d("MainActivity", "4. advertiser.startAdvertising() 호출")
                        advertiser.startAdvertising(shinhanData)
                        
                        Log.d("MainActivity", "BLE 광고 시작 완료 - 송금코드: ${bleTransferCodeDto.transferCode}")
                        isStartingAdvertising = false // 성공 시 플래그 리셋
                        Log.d("MainActivity", "isStartingAdvertising = false로 리셋")
                        
                    }.onFailure { exception ->
                        Log.e("MainActivity", "송금코드 생성 실패", exception)
                        showError("송금코드 생성에 실패했습니다: ${exception.message}")
                        isStartingAdvertising = false // 실패 시 플래그 리셋
                    }
                    
                
                
            } catch (e: Exception) {
                Log.e("MainActivity", "BLE 광고 시작 중 오류", e)
                showError("BLE 광고 시작 중 오류가 발생했습니다: ${e.message}")
                isStartingAdvertising = false // 실패 시 플래그 리셋
                Log.d("MainActivity", "예외 발생으로 isStartingAdvertising = false")
            }
        }
    }
    
    /**
     * BLE 광고 중지
     */
    private fun stopAdvertising() {
        advertiser.stopAdvertising()
        isStartingAdvertising = false // 광고 중지 시 플래그 리셋
    }
    
    /**
     * 에러 메시지 표시
     */
    private fun showError(message: String) {
        Log.e("MainActivity", "에러 발생: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 사용자 선택 시 처리 - 계좌 선택 화면으로 이동
     */
    private fun onUserSelected(user: ScannedUser) {
        Toast.makeText(this, "Selected: ${user.deviceName}", Toast.LENGTH_SHORT).show()
        
        // 계좌 선택 화면으로 이동
        val intent = Intent(this, AccountSelectionActivity::class.java).apply {
            putExtra(AccountSelectionActivity.EXTRA_SELECTED_USER, user)
        }
        startActivity(intent)
        
        // TODO: repository를 사용하여 사용자 정보 조회 또는 저장
    }
    
    /**
     * BLE 스캔 시작
     */
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
        
        // 사용자 목록 초기화
        scannedUsers.clear()
        scannedUserAdapter.updateUsers(emptyList()) // 빈 리스트로 초기화
        proximityView.clearUsers()
        
        // Ensure bottom sheet stays visible even when clearing the list
        bottomSheet.post {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                Log.d("MainActivity", "Restored bottom sheet to COLLAPSED after clearing users")
            }
        }
        
        // Nordic Scanner Compat 스캔 설정 - 더 공격적인 스캔
        val settings = NordicScanSettings.Builder()
            .setLegacy(true)  // 호환성 우선: 레거시 광고 수신 (확장 광고 미지원 단말 고려)
            .setScanMode(NordicScanSettings.SCAN_MODE_LOW_LATENCY)  // 최대 성능 모드
            .setReportDelay(0)  // 즉시 결과 전송 (배치 없음)
            .setMatchMode(NordicScanSettings.MATCH_MODE_AGGRESSIVE) // 공격적 매칭
            .setNumOfMatches(NordicScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) // 최대 광고 매칭
            .build()
        
        // 스캔 필터 설정 - 신한은행 서비스 UUID로 필터링
        val filters = ArrayList<NordicScanFilter>()
        val shinhanServiceUuid = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        filters.add(NordicScanFilter.Builder()
            .setServiceUuid(ParcelUuid(shinhanServiceUuid))
            .build())
        Log.d("MainActivity", "Starting filtered scan with Shinhan service UUID: $shinhanServiceUuid")
        
        try {
            Log.d("BLE_SCAN_START", "=== STARTING BLE SCAN ===")
            Log.d("BLE_SCAN_START", "Scanner: ${scanner.javaClass.simpleName}")
            Log.d("BLE_SCAN_START", "Filters count: ${filters.size}")
            Log.d("BLE_SCAN_START", "Filter UUID: ${filters.firstOrNull()?.serviceUuid}")
            Log.d("BLE_SCAN_START", "Scan Mode: ${settings.scanMode}")
//            Log.d("BLE_SCAN_START", "Report Delay: ${settings.reportDelay}ms")
            Log.d("BLE_SCAN_START", "Match Mode: ${settings.matchMode}")
            Log.d("BLE_SCAN_START", "Legacy: ${settings.legacy}")
            Log.d("BLE_SCAN_START", "===========================")
            
            // Nordic Scanner Compat로 스캔 시작
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true
            
            // 동시에 신한은행 송금코드 광고도 시작
            Log.d("BLE_SCAN_START", "스캔 시작 후 광고 상태 확인 - isAdvertising: $isAdvertising")
            if (!isAdvertising) {
                Log.d("BLE_SCAN_START", "광고가 시작되지 않았음, startAdvertising() 호출")
                startAdvertising()
            } else {
                Log.d("BLE_SCAN_START", "광고가 이미 시작됨, startAdvertising() 호출 스킵")
            }
            
            // Set up scan timeout
            setupScanTimeout()
            
            updateUI()
            Log.d("BLE_SCAN_START", "✅ SCAN STARTED with ${SCAN_TIMEOUT_MS/1000}s timeout")
            Toast.makeText(this, "스캔 시작 (${SCAN_TIMEOUT_MS/1000}초 후 자동 중지)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("BLE_SCAN_START", "❌ SCAN START FAILED: ${e.message}", e)
            Toast.makeText(this, getString(R.string.failed_to_start_scan, e.message), Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 스캔 타임아웃 설정
     */
    private fun setupScanTimeout() {
        // Cancel any existing timeout
        cancelScanTimeout()
        
        scanTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
        scanTimeoutRunnable = Runnable {
            Log.d("MainActivity", "Scan timeout reached, stopping scan")
            if (isScanning) {
                stopScanning()
                Toast.makeText(this, "스캔 완료 (타임아웃)", Toast.LENGTH_SHORT).show()
            }
        }
        
        scanTimeoutHandler?.postDelayed(scanTimeoutRunnable!!, SCAN_TIMEOUT_MS)
    }
    
    /**
     * 스캔 타임아웃 취소
     */
    private fun cancelScanTimeout() {
        scanTimeoutRunnable?.let { runnable ->
            scanTimeoutHandler?.removeCallbacks(runnable)
        }
        scanTimeoutRunnable = null
        scanTimeoutHandler = null
    }
    
    /**
     * BLE 스캔 중지
     */
    private fun stopScanning() {
        if (isScanning) {
            try {
                Log.d("BLE_SCAN_STOP", "🛑 STOPPING BLE SCAN")
                Log.d("BLE_SCAN_STOP", "Total users found: ${scannedUsers.size}")
                
                scanner.stopScan(scanCallback)
                isScanning = false
                
                // Cancel scan timeout
                cancelScanTimeout()
                
                // 광고도 중지
                if (isAdvertising) {
                    Log.d("BLE_SCAN_STOP", "Also stopping advertisement")
                    stopAdvertising()
                }
                
                updateUI()
                // Bottom sheet remains visible at all times
                Log.d("BLE_SCAN_STOP", "✅ SCAN STOPPED")
                Toast.makeText(this, getString(R.string.scanning_stopped), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("BLE_SCAN_STOP", "❌ SCAN STOP FAILED: ${e.message}", e)
                Toast.makeText(this, getString(R.string.failed_to_stop_scan, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    
    
    /**
     * 스캔된 사용자를 목록에 추가하거나 업데이트
     * 
     * @param result Nordic 스캔 결과
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun addOrUpdateUser(result: NordicScanResult) {
        try {
            Log.d("MainActivity", "addOrUpdateUser called for device: ${result.device.address}")
            
            val scannedUser = ScannedUser.fromScanResult(result)
            Log.d("MainActivity", "ScannedUser created: ${scannedUser.deviceName}, isShinhanUser: ${scannedUser.isShinhanUser()}")
            
            // 기존 사용자인지 MAC 주소로 확인
            val existingIndex = scannedUsers.indexOfFirst { it.deviceAddress == result.device.address }
            
            if (existingIndex >= 0) {
                // 기존 사용자 업데이트 (RSSI 등 정보 갱신)
                scannedUsers[existingIndex] = scannedUser
                Log.d("MainActivity", "Updated existing user at index $existingIndex")
            } else {
                // 새로운 사용자 추가
                scannedUsers.add(scannedUser)
                Log.d("MainActivity", "Added new user. Total users: ${scannedUsers.size}")
                
                // Bottom sheet is always visible now, no need to show/hide based on device discovery
            }
            
            // UI 업데이트 - 리스트 복사본을 전달하여 동시성 문제 방지
            Log.d("MainActivity", "Updating UI with ${scannedUsers.size} users")
            val usersCopy = scannedUsers.toList() // 불변 복사본 생성
            Log.d("MainActivity", "Created copy with ${usersCopy.size} users")
            scannedUserAdapter.updateUsers(usersCopy)
            proximityView.updateUsers(scannedUsers)
            
            // UI 상태 업데이트도 호출
            updateUI()
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in addOrUpdateUser: ${e.message}", e)
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
     * 필요한 권한들을 요청 - 시스템 다이얼로그 표시
     */
    private fun requestPermissions() {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }
    
    /**
     * 권한이 거부되었을 때 설명 다이얼로그 표시
     */
    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Bluetooth Permissions Required")
            .setMessage("This app needs Bluetooth and Location permissions to scan for nearby devices. Please grant these permissions to use the BLE scanner.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permissions are required to scan for BLE devices", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }
    
    
    /**
     * UI 업데이트 (버튼 텍스트 및 상태 표시)
     */
    private fun updateUI() {
        val scanText = if (isScanning) getString(R.string.stop_scan) else getString(R.string.start_scan)
        val statusText = when {
            isScanning && isAdvertising -> "스캔 및 브로드캐스트 중"
            isScanning -> getString(R.string.status_scanning)
            isAdvertising -> "브로드캐스트 중"
            else -> getString(R.string.status_stopped, scannedUsers.size)
        }
        
        scanButton.text = scanText
        this.statusText.text = statusText
    }
    
    /**
     * Activity 종료 시 스캔 및 광고 중지
     */
    override fun onDestroy() {
        super.onDestroy()
        if (isScanning) {
            scanner.stopScan(scanCallback)
        }
        if (isAdvertising) {
            advertiser.stopAdvertising()
        }
        // Cancel any pending scan timeout
        cancelScanTimeout()
    }
    
    /**
     * 첫 실행 처리 및 디바이스 등록
     */
    private fun handleFirstLaunch() {
        // 첫 실행 횟수 증가
        FirstLaunchHelper.incrementLaunchCount(this)
        
        // 디바이스 등록이 필요한지 확인
        if (FirstLaunchHelper.needsDeviceRegistration(this)) {
            Log.i(TAG, "First launch detected - registering device with backend")
            registerDeviceWithBackend()
        } else {
            Log.i(TAG, "Device already registered - launch count: ${FirstLaunchHelper.getLaunchCount(this)}")
        }
    }
    
    /**
     * 백엔드에 디바이스 등록
     */
    private fun registerDeviceWithBackend() {
        lifecycleScope.launch {
            try {
                // 디바이스 등록 정보 생성 (UUID 자동 생성)
                val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(
                    context = this@MainActivity
                )
                
                Log.i(TAG, "Registering device: ${deviceRegistrationDto.deviceName}")
                Log.d(TAG, "Device info: ${DeviceInfoHelper.getDeviceSummary(this@MainActivity)}")
                
                // 백엔드 API 호출
                val result = apiRepository.registerDevice(deviceRegistrationDto)
                
                result.onSuccess { deviceInfo ->
                    Log.i(TAG, "Device registration successful: ${deviceInfo.deviceName} (ID: ${deviceInfo.id})")
                    
                    // 등록 완료 표시
                    FirstLaunchHelper.markDeviceRegistered(this@MainActivity, deviceInfo.deviceId)
                    FirstLaunchHelper.markFirstLaunchCompleted(this@MainActivity)
                    
                    // 사용자에게 알림 (선택적)
                    Toast.makeText(
                        this@MainActivity,
                        "디바이스가 등록되었습니다: ${deviceInfo.deviceName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                }.onFailure { exception ->
                    Log.e(TAG, "Device registration failed", exception)
                    
                    // 등록 실패 시에도 첫 실행은 완료로 표시 (재시도 방지)
                    FirstLaunchHelper.markFirstLaunchCompleted(this@MainActivity)
                    
                    // 사용자에게 알림 (선택적)
                    Toast.makeText(
                        this@MainActivity,
                        "디바이스 등록에 실패했습니다. 나중에 다시 시도됩니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Device registration error", e)
                FirstLaunchHelper.markFirstLaunchCompleted(this@MainActivity)
            }
        }
    }
    
}