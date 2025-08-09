package com.shinhan.ble.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * BLE 송금코드 관리 엔티티
 */
@Entity
@Table(
    name = "ble_transfer_codes",
    indexes = [
        Index(name = "idx_ble_code", columnList = "transferCode", unique = true),
        Index(name = "idx_ble_user", columnList = "userId"),
        Index(name = "idx_ble_expires_at", columnList = "expiresAt")
    ]
)
data class BleTransferCode(
    @Id
    @Column(name = "code_id", length = 36)
    val codeId: String,
    
    @Column(name = "user_id", length = 36, nullable = false)
    val userId: String,
    
    @Column(name = "transfer_code", length = 50, nullable = false, unique = true)
    val transferCode: String, // 예: "088SH123ABC456"
    
    @Column(name = "encrypted_customer_number", length = 100, nullable = false)
    val encryptedCustomerNumber: String, // 암호화된 고객번호
    
    @Column(name = "nonce", length = 50, nullable = false)
    val nonce: String, // 재사용 공격 방지용 논스
    
    @Column(name = "signature", length = 100, nullable = false)
    val signature: String, // 디지털 서명
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime, // 만료 시간 (생성 후 10분)
    
    @Column(name = "used_at")
    val usedAt: LocalDateTime? = null, // 사용된 시간
    
    // 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null
) {
    /**
     * 송금코드가 유효한지 확인
     */
    fun isValid(): Boolean {
        return isActive && 
               LocalDateTime.now().isBefore(expiresAt) && 
               usedAt == null
    }
    
    /**
     * 송금코드가 만료되었는지 확인
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    /**
     * 송금코드가 이미 사용되었는지 확인
     */
    fun isUsed(): Boolean {
        return usedAt != null
    }
    
    companion object {
        /**
         * 새로운 BLE 송금코드 생성
         */
        fun generate(
            codeId: String,
            userId: String,
            customerNumber: String
        ): BleTransferCode {
            val nonce = generateNonce()
            val transferCode = generateTransferCode(customerNumber, nonce)
            val signature = generateSignature(customerNumber, nonce)
            val encryptedCustomerNumber = encryptCustomerNumber(customerNumber)
            
            return BleTransferCode(
                codeId = codeId,
                userId = userId,
                transferCode = transferCode,
                encryptedCustomerNumber = encryptedCustomerNumber,
                nonce = nonce,
                signature = signature,
                expiresAt = LocalDateTime.now().plusMinutes(10) // 10분 후 만료
            )
        }
        
        private fun generateNonce(): String {
            return java.util.UUID.randomUUID().toString().take(8)
        }
        
        private fun generateTransferCode(customerNumber: String, nonce: String): String {
            val hash = customerNumber.hashCode().toString(16).take(6).uppercase()
            return "088SH${hash}${nonce.take(3)}"
        }
        
        private fun generateSignature(customerNumber: String, nonce: String): String {
            val data = "$customerNumber$nonce${System.currentTimeMillis()}"
            return java.security.MessageDigest.getInstance("SHA-256")
                .digest(data.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .take(16)
        }
        
        private fun encryptCustomerNumber(customerNumber: String): String {
            // 실제로는 AES 암호화 사용
            return "ENC_${customerNumber.hashCode().toString(16).uppercase()}"
        }
    }
}