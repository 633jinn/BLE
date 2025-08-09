package com.shinhan.ble

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.network.dto.AccountDto
import com.shinhan.ble.data.repository.ShinhanApiRepository
import com.shinhan.ble.data.database.BleTransferRepository
import com.shinhan.ble.utils.DeviceInfoHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 계좌 선택 화면
 * 스캔된 사용자에게 송금할 계좌를 선택하는 Activity
 */
@AndroidEntryPoint
class AccountSelectionActivity : AppCompatActivity() {
    
    private lateinit var backButton: ImageButton
    private lateinit var selectedUserName: TextView
    private lateinit var selectedUserDevice: TextView
    private lateinit var userColorIndicator: View
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var continueButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var accountAdapter: AccountAdapter
    private var selectedUser: ScannedUser? = null
    private var selectedAccount: AccountDto? = null
    
    @Inject
    lateinit var repository: ShinhanApiRepository
    @Inject
    lateinit var bleRepository: BleTransferRepository
    
    companion object {
        const val EXTRA_SELECTED_USER = "extra_selected_user"
        const val EXTRA_SELECTED_ACCOUNT = "extra_selected_account"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_account_selection)
        
        // Enable immersive mode after content view is set
        enableImmersiveMode()
        
        // Handle system bars for immersive mode
        setupSystemBars()
        
        // Intent에서 선택된 사용자 정보 가져오기 (설정에서 진입 시 null 가능)
        selectedUser = intent.getParcelableExtra(EXTRA_SELECTED_USER)
        
        initializeViews()
        setupSelectedUserInfo()
        setupAccountsList()
        loadUserAccounts()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        selectedUserName = findViewById(R.id.selectedUserName)
        selectedUserDevice = findViewById(R.id.selectedUserDevice)
        userColorIndicator = findViewById(R.id.userColorIndicator)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        continueButton = findViewById(R.id.continueButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        
        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }
        
        // 다음 버튼
        continueButton.setOnClickListener {
            proceedToAmountInput()
        }
    }
    
    private fun setupSelectedUserInfo() {
        selectedUser?.let { user ->
            selectedUserName.text = user.deviceName
            selectedUserDevice.text = user.deviceAddress
            
            // 사용자 색상 표시
            val colorDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(user.color)
            }
            userColorIndicator.background = colorDrawable
        } ?: run {
            // 설정 진입: 상단 사용자 카드 비활성화 텍스트
            selectedUserName.text = "내 계좌 설정"
            selectedUserDevice.text = "대표 계좌를 선택하세요"
        }
    }
    
    private fun setupAccountsList() {
        accountAdapter = AccountAdapter(emptyList()) { account ->
            onAccountSelected(account)
        }
        
        accountsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AccountSelectionActivity)
            adapter = accountAdapter
        }
    }
    
    private fun onAccountSelected(account: AccountDto) {
        selectedAccount = account
        continueButton.isEnabled = true
    }
    
        private fun loadUserAccounts() {
        // 로딩 상태 표시
        showLoading(true)
        
        // 실제 백엔드 API로부터 계좌 목록 로드
        lifecycleScope.launch {
            try {
                val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(this@AccountSelectionActivity)
                val userId = deviceRegistrationDto.userId
                val result = userId?.let { repository.getUserAccounts(it) }

                if (result != null) {
                    result.onSuccess { accounts ->
                        showLoading(false)
                        accountAdapter.updateAccounts(accounts)

                        if (accounts.isEmpty()) {
                            showEmptyState("사용 가능한 계좌가 없습니다.")
                        } else {
                            // 대표 계좌 선택 상태로 표시
                            val primary = userId?.let { bleRepository.getPrimaryAccount(it).getOrNull() }
                            if (primary != null) {
                                accountAdapter.setSelectedAccountId(primary.accountId)
                                selectedAccount = primary
                                continueButton.isEnabled = true
                            }
                        }
                    }.onFailure { exception ->
                        showLoading(false)
                        Log.e("AccountSelectionActivit", "계좌 목록 로드 실패", exception)

                        // 실패 시 더미 데이터로 폴백
                        val dummyAccounts = createDummyAccounts()
                        accountAdapter.updateAccounts(dummyAccounts)

                        // 사용자에게 알림
                        Toast.makeText(
                            this@AccountSelectionActivity,
                            "계좌 정보를 불러오는데 실패했습니다. 더미 데이터를 표시합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                Log.e("AccountSelectionActivit", "계좌 목록 로드 중 예외 발생", e)
                
                // 예외 발생 시 더미 데이터로 폴백
                val dummyAccounts = createDummyAccounts()
                accountAdapter.updateAccounts(dummyAccounts)
                
                Toast.makeText(
                    this@AccountSelectionActivity,
                    "네트워크 오류가 발생했습니다. 더미 데이터를 표시합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingProgressBar.visibility = View.VISIBLE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "계좌 정보를 불러오는 중입니다..."
            accountsRecyclerView.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.GONE
            emptyStateText.visibility = View.GONE
            accountsRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState(message: String) {
        loadingProgressBar.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
        emptyStateText.text = message
        accountsRecyclerView.visibility = View.GONE
    }
    
    private fun proceedToAmountInput() {
        selectedAccount?.let { account ->
            lifecycleScope.launch {
                val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(this@AccountSelectionActivity)
                val userId = deviceRegistrationDto.userId
                if (userId == null) return@launch

                if (selectedUser == null) {
                    // 설정 진입: 대표 계좌 설정하고 종료
                    bleRepository.setPrimaryAccount(userId, account.accountId)
                    Toast.makeText(this@AccountSelectionActivity, "대표 계좌가 설정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // 기존 플로우: 이체 금액 화면으로 이동
                    selectedUser?.let { user ->
                        val intent = Intent(this@AccountSelectionActivity, TransferAmountActivity::class.java).apply {
                            putExtra(TransferAmountActivity.EXTRA_SELECTED_USER, user)
                            putExtra(TransferAmountActivity.EXTRA_SELECTED_ACCOUNT, account)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
    
    /**
     * 임시 더미 계좌 데이터 생성
     * 실제 구현에서는 데이터베이스에서 가져올 예정
     */
    private fun createDummyAccounts(): List<AccountDto> {
        return listOf(
            AccountDto(
                accountId = "1",
                accountNumber = "11012345678",
                bankName = "신한은행",
                bankCode = "088",
                accountType = "입출금통장",
                balance = 1_234_567L,
                currency = "KRW",
                isActive = true,
                isPrimary = true,
                dailyTransferLimit = 10_000_000L,
                singleTransferLimit = 5_000_000L,
                createdAt = "2024-01-01T00:00:00",
                updatedAt = "2024-01-01T00:00:00"
            ),
            AccountDto(
                accountId = "2",
                accountNumber = "30212345678",
                bankName = "국민은행",
                bankCode = "004",
                accountType = "적금통장",
                balance = 5_678_900L,
                currency = "KRW",
                isActive = true,
                isPrimary = false,
                dailyTransferLimit = 10_000_000L,
                singleTransferLimit = 5_000_000L,
                createdAt = "2024-01-01T00:00:00",
                updatedAt = "2024-01-01T00:00:00"
            ),
            AccountDto(
                accountId = "3",
                accountNumber = "20812345678",
                bankName = "하나은행",
                bankCode = "081",
                accountType = "입출금통장",
                balance = 987_654L,
                currency = "KRW",
                isActive = true,
                isPrimary = false,
                dailyTransferLimit = 10_000_000L,
                singleTransferLimit = 5_000_000L,
                createdAt = "2024-01-01T00:00:00",
                updatedAt = "2024-01-01T00:00:00"
            )
        )
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
            Log.e("AccountSelectionActivit", "Failed to enable immersive mode: ${e.message}")
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
        try {
            val rootView = findViewById<android.view.View>(android.R.id.content)
            rootView?.let { view ->
                ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    
                    // 상태바와 네비게이션 바 영역까지 컨텐츠가 확장되도록 패딩 조정
                    v.setPadding(
                        systemBars.left,
                        systemBars.top, // 상태바 영역 고려
                        systemBars.right,
                        systemBars.bottom  // 네비게이션 바 영역 고려
                    )
                    insets
                }
            }
        } catch (e: Exception) {
            Log.e("AccountSelectionActivit", "Failed to setup system bars: ${e.message}")
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 포커스를 다시 얻었을 때 Immersive Mode 재적용
            try {
                enableImmersiveMode()
            } catch (e: Exception) {
                Log.e("AccountSelectionActivit", "Failed to re-enable immersive mode: ${e.message}")
            }
        }
    }
}