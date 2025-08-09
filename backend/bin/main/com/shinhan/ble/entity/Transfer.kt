package com.shinhan.ble.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 송금 거래 내역 엔티티
 */
@Entity
@Table(
    name = "transfers",
    indexes = [
        Index(name = "idx_transfer_transaction_id", columnList = "transactionId", unique = true),
        Index(name = "idx_transfer_from_account", columnList = "fromAccountId"),
        Index(name = "idx_transfer_to_account", columnList = "toAccountId"),
        Index(name = "idx_transfer_status", columnList = "status"),
        Index(name = "idx_transfer_created_at", columnList = "createdAt")
    ]
)
data class Transfer(
    @Id
    @Column(name = "transfer_id", length = 36)
    val transferId: String,
    
    @Column(name = "transaction_id", length = 50, nullable = false, unique = true)
    val transactionId: String, // 신한은행 거래 고유번호
    
    @Column(name = "from_account_id", length = 36, nullable = false)
    val fromAccountId: String, // 송금 계좌 ID
    
    @Column(name = "to_account_id", length = 36, nullable = false)
    val toAccountId: String, // 수신 계좌 ID
    
    @Column(name = "from_user_id", length = 36, nullable = false)
    val fromUserId: String, // 송금 사용자 ID
    
    @Column(name = "to_user_id", length = 36, nullable = false)
    val toUserId: String, // 수신 사용자 ID
    
    @Column(name = "transfer_code", length = 50)
    val transferCode: String? = null, // BLE 송금코드 (BLE 송금인 경우)
    
    @Column(name = "amount", nullable = false)
    val amount: Long, // 송금 금액 (원 단위)
    
    @Column(name = "fee", nullable = false)
    val fee: Long = 0L, // 수수료 (원 단위)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    val status: TransferStatus = TransferStatus.PENDING,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", length = 20, nullable = false)
    val transferType: TransferType = TransferType.BLE, // 송금 유형
    
    @Column(name = "memo", length = 200)
    val memo: String? = null, // 송금 메모
    
    @Column(name = "error_message", length = 500)
    val errorMessage: String? = null, // 실패 시 에러 메시지
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null, // 완료 시간
    
    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", insertable = false, updatable = false)
    val fromAccount: Account? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", insertable = false, updatable = false)
    val toAccount: Account? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", insertable = false, updatable = false)
    val fromUser: User? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    val toUser: User? = null
) {
    /**
     * 송금이 완료되었는지 확인
     */
    fun isCompleted(): Boolean {
        return status == TransferStatus.COMPLETED
    }
    
    /**
     * 송금이 실패했는지 확인
     */
    fun isFailed(): Boolean {
        return status == TransferStatus.FAILED
    }
    
    /**
     * BLE 송금인지 확인
     */
    fun isBleTransfer(): Boolean {
        return transferType == TransferType.BLE && !transferCode.isNullOrEmpty()
    }
}

/**
 * 송금 상태 열거형
 */
enum class TransferStatus {
    PENDING,    // 처리 중
    COMPLETED,  // 완료
    FAILED,     // 실패
    CANCELLED   // 취소
}

/**
 * 송금 유형 열거형
 */
enum class TransferType {
    BLE,        // BLE 송금
    ACCOUNT,    // 계좌번호 송금
    QR,         // QR 코드 송금
    PHONE       // 전화번호 송금
}