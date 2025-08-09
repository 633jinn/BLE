package com.shinhan.ble.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 신한은행 사용자 정보 엔티티
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_phone", columnList = "phoneNumber", unique = true),
        Index(name = "idx_user_customer_number", columnList = "customerNumber", unique = true)
    ]
)
data class User(
    @Id
    @Column(name = "user_id", length = 36)
    val userId: String,
    
    @Column(name = "customer_number", length = 20, nullable = false, unique = true)
    val customerNumber: String, // 신한은행 고객번호
    
    @Column(name = "user_name", length = 50, nullable = false)
    val userName: String,
    
    @Column(name = "phone_number", length = 15, nullable = false, unique = true)
    val phoneNumber: String,
    
    @Column(name = "email", length = 100)
    val email: String? = null,
    
    @Column(name = "password_hash", length = 255, nullable = false)
    val passwordHash: String, // 암호화된 비밀번호
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "ble_enabled", nullable = false)
    val bleEnabled: Boolean = true, // BLE 송금 서비스 활성화 여부
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // 관계 매핑
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val accounts: List<Account> = mutableListOf(),
    
    @OneToMany(mappedBy = "fromUser", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sentTransfers: List<Transfer> = mutableListOf(),
    
    @OneToMany(mappedBy = "toUser", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val receivedTransfers: List<Transfer> = mutableListOf()
) {
    /**
     * 마스킹된 사용자명 반환 (BLE용)
     * 예: "김철수" -> "김*수"
     */
    fun getMaskedName(): String {
        return when {
            userName.length <= 2 -> userName
            userName.length == 3 -> "${userName.first()}*${userName.last()}"
            else -> "${userName.first()}${"*".repeat(userName.length - 2)}${userName.last()}"
        }
    }
}