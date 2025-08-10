package com.shinhan.ble

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shinhan.ble.data.database.BleTransferRepository
import com.shinhan.ble.data.network.dto.AccountCreateRequestDto
import com.shinhan.ble.utils.DeviceInfoHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountAdditionActivity : AppCompatActivity() {
    
    @Inject
    lateinit var bleTransferRepository: BleTransferRepository
    
    private val TAG = "AccountAdditionActivity"
    
    // UI Views
    private lateinit var recyclerViewAccounts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var accountAdapter: AccountAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_addition)
        
        initViews()
        setupRecyclerView()
    }
    
    private fun initViews() {
        recyclerViewAccounts = findViewById(R.id.recyclerViewAccounts)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupRecyclerView() {
//        accountAdapter = AccountAdapter(getMockBankAccounts()) { bankAccount ->
//            createAccount(bankAccount)
//        }
//        recyclerViewAccounts.layoutManager = LinearLayoutManager(this)
//        recyclerViewAccounts.adapter = accountAdapter
    }
    
    private fun getMockBankAccounts(): List<BankAccount> {
        return listOf(
            BankAccount("국민은행", "004", "입출금통장", 1000000L, "#FFE5B4"),
            BankAccount("하나은행", "081", "적금", 340000L, "#E3F2FD"),
            BankAccount("우리은행", "020", "예금", 500000L, "#F3E5F5"),
            BankAccount("신한은행", "088", "입출금통장", 750000L, "#E8F5E8"),
            BankAccount("카카오뱅크", "090", "자유입출금", 1200000L, "#FFF3E0"),
            BankAccount("토스뱅크", "092", "입출금통장", 850000L, "#F0F4F8")
        )
    }
    
    private fun createAccount(bankAccount: BankAccount) {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                // 사용자 ID 가져오기
                val deviceRegistrationDto = DeviceInfoHelper.createDeviceRegistrationDto(this@AccountAdditionActivity)
                val userId = deviceRegistrationDto.userId ?: run {
                    Log.e(TAG, "사용자 ID가 null입니다")
                    showError("사용자 정보를 가져올 수 없습니다.")
                    setLoading(false)
                    return@launch
                }
                
                // Create account request
                val accountRequest = AccountCreateRequestDto(
                    userId = userId,
                    accountType = bankAccount.accountType,
                    bankCode = bankAccount.bankCode,
                    initialBalance = bankAccount.initialBalance
                )
                
                Log.d(TAG, "계좌 생성 요청: $accountRequest")
                
                val result = bleTransferRepository.createAccount(accountRequest)
                
                if (result.isSuccess) {
                    val account = result.getOrNull()
                    Log.d(TAG, "계좌 생성 성공: $account")
                    
                    Toast.makeText(
                        this@AccountAdditionActivity,
                        "${bankAccount.bankName} 계좌가 생성되었습니다\n계좌번호: ${account?.accountNumber}\n초기 잔액: ${String.format("%,d", bankAccount.initialBalance)}원",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // 성공시 Activity 종료
                    finish()
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "계좌 생성 실패: ${error?.message}", error)
                    showError("${bankAccount.bankName} 계좌 생성에 실패했습니다: ${error?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "계좌 생성 중 오류 발생", e)
                showError("계좌 생성 중 오류가 발생했습니다: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerViewAccounts.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}

data class BankAccount(
    val bankName: String,
    val bankCode: String,
    val accountType: String,
    val initialBalance: Long,
    val backgroundColor: String
)