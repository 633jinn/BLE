package com.shinhan.ble.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * BLE로 송출되는 신한은행 데이터 (최소한의 정보만)
 * README.md에 명시된 구조를 따라 구현
 */
@Parcelize
data class ShinhanBLEData(
    val transferCode: String,    // 예: "088SH123ABC456"
    val customerName: String,    // 예: "김*수" (마스킹 처리)
    val bankCode: String = "088" // 신한은행 고정값
) : Parcelable {
    
    companion object {
        /**
         * 현재 사용자를 위한 BLE 데이터 생성 (로컬 생성 - Deprecated)
         */
        @Deprecated("Use createFromBackendResponse instead")
        fun createForCurrentUser(
            customerName: String,
            customerNumber: String
        ): ShinhanBLEData {
            // 신한은행 송금코드 생성
            val transferCode = ShinhanTransferCode.generateForCurrentUser(customerNumber)
            
            // 이름 마스킹 처리 (김철수 -> 김*수)
            val maskedName = maskCustomerName(customerName)
            
            return ShinhanBLEData(
                transferCode = transferCode.toTransferCode(),
                customerName = maskedName
            )
        }
        
        /**
         * 백엔드 API 응답으로부터 BLE 데이터 생성
         */
        fun createFromBackendResponse(bleTransferCodeDto: com.shinhan.ble.data.network.dto.BleTransferCodeDto): ShinhanBLEData {
            return ShinhanBLEData(
                transferCode = bleTransferCodeDto.transferCode,
                customerName = bleTransferCodeDto.maskedUserName,
                bankCode = bleTransferCodeDto.bankCode
            )
        }
        
        /**
         * 고객 이름 마스킹 처리
         * 예: "김철수" -> "김*수", "홍길동" -> "홍*동"
         */
        private fun maskCustomerName(name: String): String {
            return when {
                name.length <= 2 -> name
                name.length == 3 -> "${name.first()}*${name.last()}"
                else -> "${name.first()}${"*".repeat(name.length - 2)}${name.last()}"
            }
        }
        
        /**
         * 스캔 결과에서 신한은행 데이터인지 확인
         */
        fun isValidShinhanData(transferCode: String): Boolean {
            // Remove any hyphens and check format
            val cleanedCode = transferCode.replace("-", "")
            val isValid = cleanedCode.startsWith("088") && cleanedCode.length >= 11
            
            android.util.Log.d("ShinhanBLEData", "Validating transfer code:")
            android.util.Log.d("ShinhanBLEData", "  Original: '$transferCode'")
            android.util.Log.d("ShinhanBLEData", "  Cleaned: '$cleanedCode'")
            android.util.Log.d("ShinhanBLEData", "  Starts with 088: ${cleanedCode.startsWith("088")}")
            android.util.Log.d("ShinhanBLEData", "  Length >= 11: ${cleanedCode.length >= 11} (actual: ${cleanedCode.length})")
            android.util.Log.d("ShinhanBLEData", "  Is valid: $isValid")
            
            return isValid
        }
    }
    
    /**
     * 디스플레이용 은행명
     */
    fun getBankDisplayName(): String = "신한은행"
    
    /**
     * 송금코드 검증
     */
    fun isValidTransferCode(): Boolean {
        return isValidShinhanData(transferCode)
    }
}