package com.shinhan.ble

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shinhan.ble.data.ScannedUser
import com.shinhan.ble.data.network.dto.AccountDto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransferResultActivity : AppCompatActivity() {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_result)

        val titleText: TextView = findViewById(R.id.resultTitle)
        val amountText: TextView = findViewById(R.id.resultAmount)
        val fromAccountText: TextView = findViewById(R.id.resultFromAccount)
        val toUserText: TextView = findViewById(R.id.resultToUser)
        val dateText: TextView = findViewById(R.id.resultDate)
        val doneButton: Button = findViewById(R.id.resultDoneButton)
        val statusIcon: ImageView = findViewById(R.id.resultStatusIcon)

        val user: ScannedUser? = intent.getParcelableExtra(TransferConfirmationActivity.EXTRA_SELECTED_USER)
        val account: AccountDto? = intent.getParcelableExtra(TransferConfirmationActivity.EXTRA_SELECTED_ACCOUNT)
        val amount: Long = intent.getLongExtra(TransferConfirmationActivity.EXTRA_TRANSFER_AMOUNT, 0L)
        val success: Boolean = intent.getBooleanExtra("EXTRA_TRANSFER_SUCCESS", true)

        // Header
        titleText.text = if (success) "송금 완료" else "송금 실패"
        statusIcon.setImageResource(if (success) R.drawable.ic_signal_strength else R.drawable.ic_warning)

        amountText.text = numberFormat.format(amount)
        fromAccountText.text = account?.let { "${it.bankName} ${formatAccountNumber(it.accountNumber)}" } ?: "-"
        toUserText.text = user?.deviceName ?: "-"
        dateText.text = dateFormat.format(Date())

        doneButton.setOnClickListener {
            // 메인으로 복귀
            finishAffinity()
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }
    }

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
}


