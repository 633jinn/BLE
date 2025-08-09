package com.shinhan.ble

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class BankAccountAdapter(
    private val bankAccounts: List<BankAccount>,
    private val onAccountClick: (BankAccount) -> Unit
) : RecyclerView.Adapter<BankAccountAdapter.BankAccountViewHolder>() {
    
    inner class BankAccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardViewBankAccount)
        private val textBankName: TextView = itemView.findViewById(R.id.textBankName)
        private val textAccountType: TextView = itemView.findViewById(R.id.textAccountType)
        private val textInitialBalance: TextView = itemView.findViewById(R.id.textInitialBalance)
        
        fun bind(bankAccount: BankAccount) {
            textBankName.text = bankAccount.bankName
            textAccountType.text = bankAccount.accountType
            textInitialBalance.text = "초기 금액: ${String.format("%,d", bankAccount.initialBalance)}원"
            
            // Set background color
            try {
                cardView.setCardBackgroundColor(Color.parseColor(bankAccount.backgroundColor))
            } catch (e: IllegalArgumentException) {
                // Fallback to default color if parsing fails
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }
            
            // Set click listener
            cardView.setOnClickListener {
                onAccountClick(bankAccount)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankAccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank_account, parent, false)
        return BankAccountViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BankAccountViewHolder, position: Int) {
        holder.bind(bankAccounts[position])
    }
    
    override fun getItemCount(): Int = bankAccounts.size
}