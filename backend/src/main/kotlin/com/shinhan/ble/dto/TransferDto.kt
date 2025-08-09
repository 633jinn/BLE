package com.shinhan.ble.dto

import com.shinhan.ble.entity.Transfer
import com.shinhan.ble.entity.TransferStatus
import com.shinhan.ble.entity.TransferType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 송금 관련 DTO 클래스들
 */

/**
 * 송금 요청 DTO (BLE 송금코드 기반)
 */
data class TransferRequestDto(
    @field:NotBlank(message = "송금 계좌 ID는 필수입니다")
    val fromAccountId: String,
    
    @field:NotBlank(message = "송금코드는 필수입니다")
    val toTransferCode: String, // BLE에서 수신한 신한은행 송금코드
    
    @field:NotNull(message = "송금 금액은 필수입니다")
    @field:Min(value = 1000, message = "최소 송금 금액은 1,000원입니다")
    val amount: Long,
    
    @field:Size(max = 200, message = "메모는 200자 이내로 입력해주세요")
    val memo: String? = null,
    
    @field:NotBlank(message = "인증 토큰은 필수입니다")
    val authToken: String // 인증 토큰 (쏠 비밀번호/생체인증 후 받는 토큰)
)

/**
 * 일반 송금 요청 DTO (계좌번호 기반)
 */
data class AccountTransferRequestDto(
    @field:NotBlank(message = "송금 계좌 ID는 필수입니다")
    val fromAccountId: String,
    
    @field:NotBlank(message = "수취 계좌번호는 필수입니다")
    val toAccountNumber: String,
    
    @field:NotNull(message = "송금 금액은 필수입니다")
    @field:Min(value = 1000, message = "최소 송금 금액은 1,000원입니다")
    val amount: Long,
    
    @field:Size(max = 200, message = "메모는 200자 이내로 입력해주세요")
    val memo: String? = null,
    
    @field:NotBlank(message = "인증 토큰은 필수입니다")
    val authToken: String
)

/**
 * 송금 처리 결과 DTO (간단한 성공/실패 응답용)
 */
data class TransferProcessResultDto(
    val success: Boolean,
    val transactionId: String?,
    val message: String
)

/**
 * 송금 결과 DTO
 */
data class TransferResultDto(
    val transferId: String,
    val transactionId: String,
    val fromAccountNumber: String,
    val toAccountNumber: String,
    val fromUserName: String,
    val toUserName: String,
    val amount: Long,
    val fee: Long,
    val status: String,
    val transferType: String,
    val memo: String?,
    val createdAt: String,
    val completedAt: String?,
    val errorMessage: String?
) {
    companion object {
        fun from(transfer: Transfer): TransferResultDto {
            return TransferResultDto(
                transferId = transfer.transferId,
                transactionId = transfer.transactionId,
                fromAccountNumber = transfer.fromAccount?.accountNumber ?: "",
                toAccountNumber = transfer.toAccount?.accountNumber ?: "",
                fromUserName = transfer.fromUser?.userName ?: "",
                toUserName = transfer.toUser?.userName ?: "",
                amount = transfer.amount,
                fee = transfer.fee,
                status = transfer.status.name,
                transferType = transfer.transferType.name,
                memo = transfer.memo,
                createdAt = transfer.createdAt.toString(),
                completedAt = transfer.completedAt?.toString(),
                errorMessage = transfer.errorMessage
            )
        }
    }
}

/**
 * 송금 내역 요약 DTO
 */
data class TransferHistoryDto(
    val transferId: String,
    val transactionId: String,
    val amount: Long,
    val fee: Long,
    val status: String,
    val transferType: String,
    val otherPartyName: String, // 상대방 이름
    val otherPartyAccountNumber: String, // 상대방 계좌번호
    val isOutgoing: Boolean, // true: 송금, false: 입금
    val memo: String?,
    val createdAt: String
) {
    companion object {
        fun from(transfer: Transfer, currentUserId: String): TransferHistoryDto {
            val isOutgoing = transfer.fromUserId == currentUserId
            return TransferHistoryDto(
                transferId = transfer.transferId,
                transactionId = transfer.transactionId,
                amount = transfer.amount,
                fee = transfer.fee,
                status = transfer.status.name,
                transferType = transfer.transferType.name,
                otherPartyName = if (isOutgoing) 
                    transfer.toUser?.userName ?: "" 
                else 
                    transfer.fromUser?.userName ?: "",
                otherPartyAccountNumber = if (isOutgoing) 
                    transfer.toAccount?.getMaskedAccountNumber() ?: "" 
                else 
                    transfer.fromAccount?.getMaskedAccountNumber() ?: "",
                isOutgoing = isOutgoing,
                memo = transfer.memo,
                createdAt = transfer.createdAt.toString()
            )
        }
    }
}

/**
 * 송금코드 검증 요청 DTO
 */
data class TransferCodeValidationDto(
    @field:NotBlank(message = "송금코드는 필수입니다")
    val transferCode: String
)

/**
 * 송금코드 검증 결과 DTO
 */
data class TransferCodeValidationResultDto(
    val isValid: Boolean,
    val userName: String?,
    val bankName: String?,
    val maskedAccountNumber: String?,
    val expiresAt: String?,
    val errorMessage: String? = null
)

/**
 * BLE 송금코드 생성 결과 DTO
 */
data class BleTransferCodeDto(
    val transferCode: String,
    val maskedUserName: String,
    val bankCode: String,
    val expiresAt: String
)

/**
 * BLE 송금 요청 DTO
 */
data class BleTransferRequestDto(
    val fromAccountId: String,
    val toTransferCode: String,
    val amount: Long,
    val memo: String? = null,
    val authToken: String
)

/**
 * BLE 송금코드 생성 요청 DTO
 */
data class BleTransferCodeGenerationDto(
    val userId: String,
    val accountId: String,
    val validMinutes: Int? = 5,
    val amount: Long? = null,
    val description: String? = null
)

/**
 * BLE 송금코드 검증 요청 DTO
 */
data class BleTransferCodeValidationRequestDto(
    val transferCode: String
)

/**
 * BLE 송금코드 검증 결과 DTO
 */
data class BleTransferCodeValidationResult(
    val isValid: Boolean,
    val userName: String?,
    val bankName: String?,
    val maskedAccountNumber: String?,
    val expiresAt: String?,
    val errorMessage: String? = null
)

/**
 * 송금 상세 정보 DTO
 */
data class TransferDetailDto(
    val transferId: String,
    val transactionId: String,
    val fromAccountNumber: String,
    val toAccountNumber: String,
    val fromUserName: String,
    val toUserName: String,
    val amount: Long,
    val fee: Long,
    val status: String,
    val transferType: String,
    val transferCode: String?,
    val memo: String?,
    val errorMessage: String?,
    val createdAt: String,
    val completedAt: String?
) {
    companion object {
        fun from(transfer: Transfer): TransferDetailDto {
            return TransferDetailDto(
                transferId = transfer.transferId,
                transactionId = transfer.transactionId,
                fromAccountNumber = transfer.fromAccount?.accountNumber ?: "",
                toAccountNumber = transfer.toAccount?.accountNumber ?: "",
                fromUserName = transfer.fromUser?.userName ?: "",
                toUserName = transfer.toUser?.userName ?: "",
                amount = transfer.amount,
                fee = transfer.fee,
                status = transfer.status.name,
                transferType = transfer.transferType.name,
                transferCode = transfer.transferCode,
                memo = transfer.memo,
                errorMessage = transfer.errorMessage,
                createdAt = transfer.createdAt.toString(),
                completedAt = transfer.completedAt?.toString()
            )
        }
    }
}

/**
 * 송금 통계 DTO
 */
data class TransferStatisticsDto(
    val userId: String,
    val year: Int,
    val month: Int,
    val totalCount: Long,
    val totalAmount: Long
)