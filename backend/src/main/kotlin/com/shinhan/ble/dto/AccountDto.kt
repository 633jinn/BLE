package com.shinhan.ble.dto

import com.shinhan.ble.entity.Account

/**
 * 계좌 관련 DTO 클래스들
 */

/**
 * 계좌 정보 응답 DTO
 */
data class AccountDto(
    val accountId: String,
    val accountNumber: String,
    val accountType: String,
    val bankName: String,
    val bankCode: String,
    val balance: Long,
    val currency: String,
    val isActive: Boolean,
    val isPrimary: Boolean,
    val dailyTransferLimit: Long,
    val singleTransferLimit: Long,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(account: Account): AccountDto {
            return AccountDto(
                accountId = account.accountId,
                accountNumber = account.accountNumber,
                accountType = account.accountType,
                bankName = account.bankName,
                bankCode = account.bankCode,
                balance = account.balance,
                currency = account.currency,
                isActive = account.isActive,
                isPrimary = account.isPrimary,
                dailyTransferLimit = account.dailyTransferLimit,
                singleTransferLimit = account.singleTransferLimit,
                createdAt = account.createdAt.toString(),
                updatedAt = account.updatedAt.toString()
            )
        }
    }
}

/**
 * 계좌 목록 응답 DTO (간소화된 정보)
 */
data class AccountSummaryDto(
    val accountId: String,
    val accountNumber: String,
    val accountType: String,
    val bankName: String,
    val balance: Long,
    val currency: String,
    val isActive: Boolean,
    val isPrimary: Boolean
) {
    companion object {
        fun from(account: Account): AccountSummaryDto {
            return AccountSummaryDto(
                accountId = account.accountId,
                accountNumber = account.accountNumber,
                accountType = account.accountType,
                bankName = account.bankName,
                balance = account.balance,
                currency = account.currency,
                isActive = account.isActive,
                isPrimary = account.isPrimary
            )
        }
    }
}

/**
 * 계좌 잔액 응답 DTO
 */
data class AccountBalanceDto(
    val accountId: String,
    val balance: Long,
    val currency: String,
    val lastUpdated: String
) {
    companion object {
        fun from(account: Account): AccountBalanceDto {
            return AccountBalanceDto(
                accountId = account.accountId,
                balance = account.balance,
                currency = account.currency,
                lastUpdated = account.updatedAt.toString()
            )
        }
    }
}

/**
 * 송금 유효성 검사 요청 DTO
 */
data class TransferValidationRequest(
    val userId: String,
    val amount: Long
)

/**
 * 송금 유효성 검사 결과 DTO
 */
data class TransferValidationResult(
    val isValid: Boolean,
    val availableBalance: Long,
    val dailyLimitRemaining: Long,
    val singleLimitExceeded: Boolean,
    val errorMessage: String? = null
)

/**
 * 계좌 생성 요청 DTO
 */
data class AccountCreationDto(
    val userId: String,
    val accountType: String,
    val bankCode: String,
    val initialBalance: Long = 0L
)

/**
 * 송금 한도 업데이트 DTO
 */
data class TransferLimitsUpdateDto(
    val userId: String,
    val singleLimit: Long,
    val dailyLimit: Long
)

// 기존 코드와의 호환성을 위한 타입 별칭들
typealias AccountInfoDto = AccountDto