package com.shinhan.ble.data

import java.security.MessageDigest
import java.util.*

/**
 * 신한은행 송금코드 데이터 클래스
 * README.md에 명시된 구조를 따라 구현
 */
data class ShinhanTransferCode(
    val customerNumber: String,  // 암호화된 신한은행 고객번호
    val timestamp: Long,         // 생성 시간 (유효기간 10분)
    val nonce: String,          // 재사용 공격 방지
    val bankCode: String = "088", // 신한은행 코드 (088)
    val signature: String       // 신한은행 디지털 서명
) {
    companion object {
        private const val VALIDITY_DURATION = 10 * 60 * 1000L // 10분 (밀리초)
        
        /**
         * 현재 사용자를 위한 송금코드 생성
         */
        fun generateForCurrentUser(customerNumber: String): ShinhanTransferCode {
            val timestamp = System.currentTimeMillis()
            val nonce = UUID.randomUUID().toString().take(8)
            
            // 간단한 시그니처 생성 (실제로는 신한은행 서버에서 생성)
            val signature = generateSignature(customerNumber, timestamp, nonce)
            
            return ShinhanTransferCode(
                customerNumber = encryptCustomerNumber(customerNumber),
                timestamp = timestamp,
                nonce = nonce,
                signature = signature
            )
        }
        
        /**
         * 고객번호 암호화 (실제로는 신한은행 암호화 방식 사용)
         */
        private fun encryptCustomerNumber(customerNumber: String): String {
            return "SH${customerNumber.hashCode().toString(16).take(8).uppercase()}"
        }
        
        /**
         * 디지털 서명 생성 (실제로는 신한은행 서명 알고리즘 사용)
         */
        private fun generateSignature(customerNumber: String, timestamp: Long, nonce: String): String {
            val data = "$customerNumber$timestamp$nonce"
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(data.toByteArray())
            return hash.joinToString("") { "%02x".format(it) }.take(16)
        }
    }
    
    /**
     * 송금코드가 유효한지 확인
     */
    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - timestamp) <= VALIDITY_DURATION
    }
    
    /**
     * BLE 브로드캐스트용 간략한 송금코드 생성
     */
    fun toTransferCode(): String {
        return "${bankCode}${customerNumber.take(6)}${nonce.take(3)}"
    }
}