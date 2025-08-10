package com.shinhan.ble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shinhan.ble.data.network.dto.AccountDto
import java.text.NumberFormat
import java.util.*

/**
 * 계좌 목록을 표시하는 RecyclerView 어댑터
 */
class AccountAdapter(
    private var accounts: List<AccountDto>,
    private val onAccountSelected: (AccountDto) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var selectedAccountId: String? = null
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankIcon: ImageView = itemView.findViewById(R.id.bankIcon)
        val bankName: TextView = itemView.findViewById(R.id.bankName)
        val accountType: TextView = itemView.findViewById(R.id.accountType)
        val accountNumber: TextView = itemView.findViewById(R.id.accountNumber)
        val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)
        val selectionRadio: RadioButton = itemView.findViewById(R.id.selectionRadio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]

        holder.bankName.text = account.bankName
        holder.accountType.text = account.accountType
        holder.accountNumber.text = formatAccountNumber(account.accountNumber)
        holder.accountBalance.text = "잔액 ${numberFormat.format(account.balance)}원"

        // 선택 상태 표시 (대표 계좌면 초기 선택 상태로도 표시)
        holder.selectionRadio.isChecked = (account.accountId == selectedAccountId) || (account.isPrimary && selectedAccountId == null)

        // 계좌 선택 처리
        holder.itemView.setOnClickListener {
            val previousSelected = selectedAccountId
            selectedAccountId = account.accountId

            // 이전 선택된 항목과 현재 선택된 항목 업데이트
            previousSelected?.let { prevId ->
                val prevIndex = accounts.indexOfFirst { it.accountId == prevId }
                if (prevIndex != -1) {
                    notifyItemChanged(prevIndex)
                }
            }
            notifyItemChanged(position)

            onAccountSelected(account)
        }

        // 은행별 아이콘 설정 (실제로는 더 많은 은행 아이콘을 추가할 수 있음)
        when (account.bankName) {
            "신한은행" -> holder.bankIcon.setImageResource(R.drawable.ic_bank)
            "국민은행" -> holder.bankIcon.setImageResource(R.drawable.ic_bank)
            "하나은행" -> holder.bankIcon.setImageResource(R.drawable.ic_bank)
            else -> holder.bankIcon.setImageResource(R.drawable.ic_bank)
        }
    }

    override fun getItemCount(): Int = accounts.size

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
     * 계좌 목록 업데이트
     */
    fun updateAccounts(newAccounts: List<AccountDto>) {
        accounts = newAccounts
        selectedAccountId = null
        notifyDataSetChanged()
    }

    /**
     * 선택된 계좌 반환
     */
    fun getSelectedAccount(): AccountDto? {
        return selectedAccountId?.let { id ->
            accounts.find { it.accountId == id }
        }
    }

    /**
     * 외부에서 선택 계좌를 지정 (대표 계좌 사전 선택 등)
     */
    fun setSelectedAccountId(accountId: String?) {
        selectedAccountId = accountId
        notifyDataSetChanged()
    }
}