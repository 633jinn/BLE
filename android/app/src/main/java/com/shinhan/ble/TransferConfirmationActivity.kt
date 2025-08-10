package com.shinhan.ble

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.network.dto.AccountDto
import com.shinhan.ble.data.network.dto.BleTransferRequestDto
import com.shinhan.ble.data.repository.ShinhanApiRepository
import com.shinhan.ble.utils.DeviceInfoHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 거래 확인 화면
 * 이체 정보를 최종 확인하고 거래를 실행하는 Activity
 */
@AndroidEntryPoint
class TransferConfirmationActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: ShinhanApiRepository

    private lateinit var backButton: ImageButton
    private lateinit var transferAmount: TextView
    private lateinit var fromAccountInfo: TextView
    private lateinit var fromAccountBalance: TextView
    private lateinit var fromBankIcon: ImageView
    private lateinit var toUserName: TextView
    private lateinit var toUserDevice: TextView
    private lateinit var toUserColorIndicator: View
    private lateinit var transactionDate: TextView
    private lateinit var cancelButton: Button
    private lateinit var transferButton: Button

    private var selectedUser: ScannedUser? = null
    private var selectedAccount: AccountDto? = null
    private var transferAmountValue: Long = 0L

    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    companion object {
        const val EXTRA_SELECTED_USER = "extra_selected_user"
        const val EXTRA_SELECTED_ACCOUNT = "extra_selected_account"
        const val EXTRA_TRANSFER_AMOUNT = "extra_transfer_amount"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transfer_confirmation)

        // Enable immersive mode after content view is set
        enableImmersiveMode()

        // Handle system bars for immersive mode
        setupSystemBars()

        // Intent에서 데이터 가져오기
        selectedUser = intent.getParcelableExtra(EXTRA_SELECTED_USER)
        selectedAccount = intent.getParcelableExtra(EXTRA_SELECTED_ACCOUNT)
        transferAmountValue = intent.getLongExtra(EXTRA_TRANSFER_AMOUNT, 0L)

        if (selectedUser == null || selectedAccount == null || transferAmountValue <= 0L) {
            finish()
            return
        }

        initializeViews()
        setupTransferInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        transferAmount = findViewById(R.id.transferAmount)
        fromAccountInfo = findViewById(R.id.fromAccountInfo)
        fromAccountBalance = findViewById(R.id.fromAccountBalance)
        fromBankIcon = findViewById(R.id.fromBankIcon)
        toUserName = findViewById(R.id.toUserName)
        toUserDevice = findViewById(R.id.toUserDevice)
        toUserColorIndicator = findViewById(R.id.toUserColorIndicator)
        transactionDate = findViewById(R.id.transactionDate)
        cancelButton = findViewById(R.id.cancelButton)
        transferButton = findViewById(R.id.transferButton)
    }

    private fun setupTransferInfo() {
        // 이체 금액 표시
        transferAmount.text = numberFormat.format(transferAmountValue)

        // 보내는 계좌 정보
        selectedAccount?.let { account ->
            fromAccountInfo.text = "${account.bankName} ${formatAccountNumber(account.accountNumber)}"
            fromAccountBalance.text = "잔액 ${numberFormat.format(account.balance)}원"
        }

        // 받는 사용자 정보
        selectedUser?.let { user ->
            toUserName.text = user.deviceName
            toUserDevice.text = user.deviceAddress

            // 사용자 색상 표시
            val colorDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(user.color)
            }
            toUserColorIndicator.background = colorDrawable
        }

        // 거래 일시
        transactionDate.text = dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            showCancelConfirmDialog()
        }

        transferButton.setOnClickListener {
            showTransferConfirmDialog()
        }
    }

    private fun showCancelConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("이체 취소")
            .setMessage("이체를 취소하시겠습니까?")
            .setPositiveButton("취소하기") { _, _ ->
                // 메인 화면으로 돌아가기
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("계속하기") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTransferConfirmDialog() {
        val message = """
            ${numberFormat.format(transferAmountValue)}원을
            ${selectedUser?.deviceName}에게
            이체하시겠습니까?
            
            이 작업은 취소할 수 없습니다.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("이체 확인")
            .setMessage(message)
            .setPositiveButton("이체하기") { _, _ ->
                executeTransfer()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun executeTransfer() {
        selectedAccount?.let { account ->
            selectedUser?.let { user ->
                // 로딩 상태 표시
                transferButton.isEnabled = false
                transferButton.text = "송금 처리 중..."

                lifecycleScope.launch {
                    try {
                        // 단계 1: BLE로 받은 송금코드 추출
                        val transferCode = user.transferCode ?: run {
                            showError("송금 코드를 찾을 수 없습니다.\nBLE로 발견된 사용자가 아닙니다.")
                            return@launch
                        }

                        Log.d("TransferConfirmation", "BLE로 받은 솨금코드: $transferCode")

                        // 단계 2: 송금코드 유효성 검증 (신한은행 서버)
                        val validationResult = repository.validateTransferCode(transferCode)

                        validationResult.onSuccess { validation ->
                            if (!validation.isValid) {
                                showError("유효하지 않은 송금코드입니다.\n${validation.errorMessage ?: "코드가 만료되었거나 잘못된 코드입니다."}")
                                Log.d("TransferConfirmation", "BLE로 받은 솨금에러: $validation.errorMessage")
                                return@launch
                            }

                            // 단계 3: 신한은행 서버를 통한 실제 송금 처리
                            processTransferViaShinhanSystem(account, transferCode, validation)

                        }.onFailure { exception ->
                            Log.e("TransferConfirmation", "송금코드 검증 실패", exception)
                            showError("송금코드 검증에 실패했습니다.\n${exception.message}")
                        }

                    } catch (e: Exception) {
                        Log.e("TransferConfirmation", "송금 처리 중 예외 발생", e)
                        showError("네트워크 오류가 발생했습니다.\n${e.message}")
                    } finally {
                        // UI 상태 복원
                        transferButton.isEnabled = true
                        transferButton.text = "이체하기"
                    }
                }
            }
        }
    }

    private fun proceedToSuccess() {
        val intent = Intent(this, TransferResultActivity::class.java).apply {
            putExtra(EXTRA_SELECTED_USER, selectedUser)
            putExtra(EXTRA_SELECTED_ACCOUNT, selectedAccount)
            putExtra(EXTRA_TRANSFER_AMOUNT, transferAmountValue)
            putExtra("EXTRA_TRANSFER_SUCCESS", true)
        }
        startActivity(intent)
        finish()
    }

    /**
     * 계좌번호 포맷팅 (예: 110123456789 -> 110-123-456789)
     */
    private fun formatAccountNumber(accountNumber: String): String {
        return when {
            accountNumber.length >= 10 -> {
                "${accountNumber.substring(0, 3)}-${accountNumber.substring(3, 6)}-${accountNumber.substring(6)}"
            }
            accountNumber.length >= 6 -> {
                "${accountNumber.substring(0, 3)}-${accountNumber.substring(3)}"
            }
            else -> accountNumber
        }
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
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }

            // Status bar와 navigation bar를 투명하게 설정
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

        } catch (e: Exception) {
            Log.e("TransferConfirmationAc", "Failed to enable immersive mode: ${e.message}")
            // Fallback to basic fullscreen mode
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    /**
     * 시스템 바 처리 설정
     */
    private fun setupSystemBars() {
        try {
            val rootView = findViewById<View>(android.R.id.content)
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
            Log.e("TransferConfirmat", "Failed to setup system bars: ${e.message}")
        }
    }

    /**
     * 신한은행 시스템을 통한 실제 송금 처리
     * 카카오페이의 "내 주변 송금" 방식과 동일:
     * - BLE로 사용자 발견 및 송금코드 수신
     * - 신한은행 보안 인프라로 실제 계좌이체 수행
     */
    private suspend fun processTransferViaShinhanSystem(
        fromAccount: AccountDto,
        transferCode: String,
        validation: com.shinhan.ble.data.network.dto.BleTransferCodeValidationResult
    ) {
        try {
            val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(this@TransferConfirmationActivity)
            val userId = deviceRegistrationDto.userId ?: ""

            // 신한은행 코어뱅킹 시스템으로 송금 요청
            val bleTransferRequest = BleTransferRequestDto(
                fromAccountId = fromAccount.accountId,
                toTransferCode = transferCode, // BLE로 받은 송금코드
                amount = transferAmountValue,
                memo = "SOL BLE 내 주변 송금", // 신한은행 메모
                authToken = userId // 사용자 인증 토큰
            )

            Log.d("TransferConfirmation", "신한은행 서버로 송금 요청: ${validation.userName}에게 ${transferAmountValue}원")

            // 신한은행 API 호출
            val result = repository.processBleTransfer(bleTransferRequest, userId)

            result.onSuccess { transferResult ->
                Log.i("TransferConfirmation", "송금 성공: ${transferResult.transactionId}")

                if (transferResult.success) {
                    // 성공 메시지에 수신자 정보 포함
                    val successMessage = buildString {
                        append("송금이 완료되었습니다!\n\n")
                        append("받는 사람: ${validation.userName}\n")
                        append("받는 은행: ${validation.bankName}\n")
                        append("송금 금액: ${numberFormat.format(transferAmountValue)}원\n")
                    }

                    showSuccessDialog(successMessage)
                } else {
                    showError("송금 처리에 실패했습니다.\n${transferResult.message}")
                }

            }.onFailure { exception ->
                Log.e("TransferConfirmation", "신한은행 송금 실패", exception)
                showError("송금 처리에 실패했습니다.\n${exception.message}\n\n신한은행 코어뱅킹 시스템에서 오류가 발생했습니다.")
            }

        } catch (e: Exception) {
            Log.e("TransferConfirmation", "신한은행 송금 처리 중 예외", e)
            throw e
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("송금 오류")
            .setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("송금 완료")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                proceedToSuccess()
            }
            .setCancelable(false)
            .show()
    }

    @SuppressLint("LongLogTag")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 포커스를 다시 얻었을 때 Immersive Mode 재적용
            try {
                enableImmersiveMode()
            } catch (e: Exception) {
                Log.e("TransferConfirmationActivity", "Failed to re-enable immersive mode: ${e.message}")
            }
        }
    }
}