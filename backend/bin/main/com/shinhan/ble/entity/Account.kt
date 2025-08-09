package com.shinhan.ble.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 신한은행 계좌 정보 엔티티
 */
@Entity
@Table(
    name = "accounts",
    indexes = [
        Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
        Index(name = "idx_account_user", columnList = "userId")
    ]
)
data class Account(
    @Id
    @Column(name = "account_id", length = 36)
    val accountId: String,
    
    @Column(name = "user_id", length = 36, nullable = false)
    val userId: String,
    
    @Column(name = "account_number", length = 20, nullable = false, unique = true)
    val accountNumber: String, // 계좌번호
    
    @Column(name = "account_type", length = 50, nullable = false)
    val accountType: String, // 계좌종류 (입출금통장, 적금 등)
    
    @Column(name = "bank_name", length = 50, nullable = false)
    val bankName: String = "신한은행",
    
    @Column(name = "bank_code", length = 10, nullable = false)
    val bankCode: String = "088", // 신한은행 코드
    
    @Column(name = "balance", nullable = false)
    val balance: Long = 0L, // 잔액 (원 단위)
    
    @Column(name = "currency", length = 10, nullable = false)
    val currency: String = "KRW", // 통화
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,
    
    @Column(name = "daily_transfer_limit", nullable = false)
    val dailyTransferLimit: Long = 10000000L, // 일일 이체 한도 (1천만원)
    
    @Column(name = "single_transfer_limit", nullable = false)
    val singleTransferLimit: Long = 2000000L, // 단일 이체 한도 (200만원)
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,
    
    @OneToMany(mappedBy = "fromAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sentTransfers: List<Transfer> = mutableListOf(),
    
    @OneToMany(mappedBy = "toAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val receivedTransfers: List<Transfer> = mutableListOf()
) {
    /**
     * 계좌번호 마스킹 처리
     * 예: "110-123-456789" -> "110-***-456789"
     */
    fun getMaskedAccountNumber(): String {
        val parts = accountNumber.split("-")
        return if (parts.size == 3) {
            "${parts[0]}-***-${parts[2]}"
        } else {
            accountNumber.take(3) + "*".repeat(maxOf(0, accountNumber.length - 6)) + accountNumber.takeLast(3)
        }
    }
    
    /**
     * 잔액 부족 체크
     */
    fun hasInsufficientBalance(amount: Long): Boolean {
        return balance < amount
    }
    
    /**
     * 이체 한도 체크
     */
    fun exceedsSingleTransferLimit(amount: Long): Boolean {
        return amount > singleTransferLimit
    }
}