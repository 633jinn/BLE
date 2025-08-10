package com.shinhan.ble

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.network.dto.AccountDto
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

/**
 * 이체 금액 입력 화면
 * 선택된 계좌에서 스캔된 사용자에게 송금할 금액을 입력하는 Activity
 */
@AndroidEntryPoint
class TransferAmountActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var fromAccountInfo: TextView
    private lateinit var fromAccountBalance: TextView
    private lateinit var fromBankIcon: ImageView
    private lateinit var toUserName: TextView
    private lateinit var toUserDevice: TextView
    private lateinit var toUserColorIndicator: View
    private lateinit var amountDisplay: TextView
    private lateinit var amount10000: Button
    private lateinit var amount50000: Button
    private lateinit var amount100000: Button
    private lateinit var clearButton: Button
    private lateinit var confirmButton: Button
    private lateinit var numberPadContainer: androidx.cardview.widget.CardView

    private var selectedUser: ScannedUser? = null
    private var selectedAccount: AccountDto? = null
    private var currentAmount: Long = 0L
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)


    companion object {
        const val EXTRA_SELECTED_USER = "extra_selected_user"
        const val EXTRA_SELECTED_ACCOUNT = "extra_selected_account"
        const val EXTRA_TRANSFER_AMOUNT = "extra_transfer_amount"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transfer_amount)

        // Enable immersive mode after content view is set
        enableImmersiveMode()

        // Handle system bars for immersive mode
        setupSystemBars()

        // Intent에서 데이터 가져오기
        selectedUser = intent.getParcelableExtra(EXTRA_SELECTED_USER)
        selectedAccount = intent.getParcelableExtra(EXTRA_SELECTED_ACCOUNT)

        if (selectedUser == null || selectedAccount == null) {
            finish()
            return
        }

        initializeViews()
        setupTransferInfo()
        setupNumberPad()
        setupClickListeners()
        updateAmountDisplay()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        fromAccountInfo = findViewById(R.id.fromAccountInfo)
        fromAccountBalance = findViewById(R.id.fromAccountBalance)
        fromBankIcon = findViewById(R.id.fromBankIcon)
        toUserName = findViewById(R.id.toUserName)
        toUserDevice = findViewById(R.id.toUserDevice)
        toUserColorIndicator = findViewById(R.id.toUserColorIndicator)
        amountDisplay = findViewById(R.id.amountDisplay)
        amount10000 = findViewById(R.id.amount10000)
        amount50000 = findViewById(R.id.amount50000)
        amount100000 = findViewById(R.id.amount100000)
        clearButton = findViewById(R.id.clearButton)
        confirmButton = findViewById(R.id.confirmButton)
        numberPadContainer = findViewById(R.id.numberPadContainer)
    }

    private fun setupTransferInfo() {
        selectedAccount?.let { account ->
            fromAccountInfo.text = "${account.bankName} ${formatAccountNumber(account.accountNumber)}"
            fromAccountBalance.text = "잔액 ${numberFormat.format(account.balance)}원"
        }

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
    }

    private fun setupNumberPad() {
        // Number pad buttons are already defined in the layout
        // Set click listeners for existing buttons
        findViewById<Button>(R.id.btn1).setOnClickListener { onNumberPadClick("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { onNumberPadClick("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { onNumberPadClick("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { onNumberPadClick("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { onNumberPadClick("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { onNumberPadClick("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { onNumberPadClick("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { onNumberPadClick("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { onNumberPadClick("9") }
        findViewById<Button>(R.id.btn0).setOnClickListener { onNumberPadClick("0") }
        findViewById<Button>(R.id.btn00).setOnClickListener { onNumberPadClick("00") }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { onNumberPadClick("⌫") }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        // 빠른 금액 버튼들
        amount10000.setOnClickListener { addAmount(10000) }
        amount50000.setOnClickListener { addAmount(50000) }
        amount100000.setOnClickListener { addAmount(100000) }

        clearButton.setOnClickListener {
            currentAmount = 0L
            updateAmountDisplay()
        }

        confirmButton.setOnClickListener {
            proceedToConfirmation()
        }
    }

    private fun onNumberPadClick(input: String) {
        when (input) {
            "⌫" -> {
                // 백스페이스 - 마지막 자리 삭제
                if (currentAmount > 0) {
                    currentAmount /= 10
                    updateAmountDisplay()
                }
            }
            "00" -> {
                // 00 입력 - 현재 금액에 100을 곱함
                val newAmount = currentAmount * 100
                if (newAmount <= 1_000_000_000L) {
                    currentAmount = newAmount
                    updateAmountDisplay()
                }
            }
            else -> {
                // 숫자 입력
                val digit = input.toIntOrNull()
                if (digit != null) {
                    // 최대 10억원으로 제한
                    val newAmount = currentAmount * 10 + digit
                    if (newAmount <= 1_000_000_000L) {
                        currentAmount = newAmount
                        updateAmountDisplay()
                    }
                }
            }
        }
    }

    private fun addAmount(amount: Long) {
        val newAmount = currentAmount + amount
        // 최대 10억원으로 제한
        if (newAmount <= 1_000_000_000L) {
            currentAmount = newAmount
            updateAmountDisplay()
        }
    }

    private fun updateAmountDisplay() {
        if (currentAmount == 0L) {
            amountDisplay.text = "0"
            confirmButton.isEnabled = false
        } else {
            amountDisplay.text = numberFormat.format(currentAmount)
            confirmButton.isEnabled = true

            // 잔액 부족 체크
            selectedAccount?.let { account ->
                if (currentAmount > account.balance) {
                    confirmButton.isEnabled = false
                    amountDisplay.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                } else {
                    confirmButton.isEnabled = true
                    amountDisplay.setTextColor(ContextCompat.getColor(this, R.color.primary_color))
                }
            }
        }
    }

    private fun proceedToConfirmation() {
        if (currentAmount > 0L) {
            selectedAccount?.let { account ->
                selectedUser?.let { user ->
                    val intent = Intent(this, TransferConfirmationActivity::class.java).apply {
                        putExtra(TransferConfirmationActivity.EXTRA_SELECTED_USER, user)
                        putExtra(TransferConfirmationActivity.EXTRA_SELECTED_ACCOUNT, account)
                        putExtra(TransferConfirmationActivity.EXTRA_TRANSFER_AMOUNT, currentAmount)
                    }
                    startActivity(intent)
                }
            }
        }
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
            Log.e("TransferAmountActivity", "Failed to enable immersive mode: ${e.message}")
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
            Log.e("TransferAmountActivity", "Failed to setup system bars: ${e.message}")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 포커스를 다시 얻었을 때 Immersive Mode 재적용
            try {
                enableImmersiveMode()
            } catch (e: Exception) {
                Log.e("TransferAmountActivity", "Failed to re-enable immersive mode: ${e.message}")
            }
        }
    }
}