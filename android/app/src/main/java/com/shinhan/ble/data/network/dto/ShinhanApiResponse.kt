package com.shinhan.ble.data.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 신한은행 API 공통 응답 형식
 */
data class ShinhanApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * 계좌 정보 DTO
 */
@Parcelize
data class AccountDto(
    @SerializedName("accountId")
    val accountId: String,
    
    @SerializedName("accountNumber")
    val accountNumber: String,
    
    @SerializedName("accountType")
    val accountType: String,
    
    @SerializedName("bankName")
    val bankName: String,
    
    @SerializedName("bankCode")
    val bankCode: String,
    
    @SerializedName("balance")
    val balance: Long,
    
    @SerializedName("currency")
    val currency: String = "KRW",
    
    @SerializedName("isActive")
    val isActive: Boolean,
    
    @SerializedName("isPrimary")
    val isPrimary: Boolean,
    
    
    @SerializedName("dailyTransferLimit")
    val dailyTransferLimit: Long,
    
    @SerializedName("singleTransferLimit")
    val singleTransferLimit: Long,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
) : Parcelable

/**
 * BLE 송금 요청 DTO
 */
data class BleTransferRequestDto(
    @SerializedName("fromAccountId")
    val fromAccountId: String,
    
    @SerializedName("toTransferCode")
    val toTransferCode: String,
    
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("memo")
    val memo: String? = null,
    
    @SerializedName("authToken")
    val authToken: String
)

/**
 * 송금 처리 결과 DTO (백엔드 응답용)
 */
data class TransferProcessResultDto(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("transactionId")
    val transactionId: String?,
    
    @SerializedName("message")
    val message: String
)

/**
 * 송금 결과 DTO
 */
data class TransferResultDto(
    @SerializedName("transferId")
    val transferId: String,
    
    @SerializedName("transactionId")
    val transactionId: String,
    
    @SerializedName("fromAccountNumber")
    val fromAccountNumber: String,
    
    @SerializedName("toAccountNumber")
    val toAccountNumber: String,
    
    @SerializedName("fromUserName")
    val fromUserName: String,
    
    @SerializedName("toUserName")
    val toUserName: String,
    
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("fee")
    val fee: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("transferType")
    val transferType: String,
    
    @SerializedName("memo")
    val memo: String?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("completedAt")
    val completedAt: String?,
    
    @SerializedName("errorMessage")
    val errorMessage: String?
)

/**
 * BLE 송금코드 생성 요청 DTO
 */
data class BleTransferCodeGenerationDto(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("accountId")
    val accountId: String
)

/**
 * BLE 송금코드 DTO
 */
data class BleTransferCodeDto(
    @SerializedName("transferCode")
    val transferCode: String,
    
    @SerializedName("maskedUserName")
    val maskedUserName: String,
    
    @SerializedName("bankCode")
    val bankCode: String,
    
    @SerializedName("expiresAt")
    val expiresAt: String
)

/**
 * BLE 송금코드 검증 요청 DTO
 */
data class BleTransferCodeValidationRequestDto(
    @SerializedName("transferCode")
    val transferCode: String
)

/**
 * BLE 송금코드 검증 결과 DTO
 */
data class BleTransferCodeValidationResult(
    @SerializedName("isValid")
    val isValid: Boolean,
    
    @SerializedName("userName")
    val userName: String?,
    
    @SerializedName("bankName")
    val bankName: String?,
    
    @SerializedName("maskedAccountNumber")
    val maskedAccountNumber: String?,
    
    @SerializedName("expiresAt")
    val expiresAt: String?,
    
    @SerializedName("errorMessage")
    val errorMessage: String?
)

/**
 * 송금 내역 DTO
 */
data class TransferHistoryDto(
    @SerializedName("transferId")
    val transferId: String,
    
    @SerializedName("transactionId")
    val transactionId: String,
    
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("fee")
    val fee: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("transferType")
    val transferType: String,
    
    @SerializedName("otherPartyName")
    val otherPartyName: String,
    
    @SerializedName("otherPartyAccountNumber")
    val otherPartyAccountNumber: String,
    
    @SerializedName("isOutgoing")
    val isOutgoing: Boolean,
    
    @SerializedName("memo")
    val memo: String?,
    
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 거래 상세 DTO
 */
data class TransferDetailDto(
    @SerializedName("transferId")
    val transferId: String,
    
    @SerializedName("transactionId")
    val transactionId: String,
    
    @SerializedName("fromAccountNumber")
    val fromAccountNumber: String,
    
    @SerializedName("toAccountNumber")
    val toAccountNumber: String,
    
    @SerializedName("fromUserName")
    val fromUserName: String,
    
    @SerializedName("toUserName")
    val toUserName: String,
    
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("fee")
    val fee: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("transferType")
    val transferType: String,
    
    @SerializedName("memo")
    val memo: String?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("completedAt")
    val completedAt: String?,
    
    @SerializedName("errorMessage")
    val errorMessage: String?
)

/**
 * 페이징 응답 DTO
 */
data class PagedResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("hasNext")
    val hasNext: Boolean,
    
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

/**
 * 디바이스 등록 요청 DTO
 */
data class DeviceRegistrationDto(
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("deviceModel")
    val deviceModel: String?,
    
    @SerializedName("osVersion")
    val osVersion: String?,
    
    @SerializedName("appVersion")
    val appVersion: String?,
    
    @SerializedName("deviceId")
    val deviceId: String? = null,
    
    @SerializedName("userId")
    val userId: String? = null
)

/**
 * 디바이스 정보 응답 DTO
 */
data class DeviceInfoDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("deviceModel")
    val deviceModel: String?,
    
    @SerializedName("osVersion")
    val osVersion: String?,
    
    @SerializedName("appVersion")
    val appVersion: String?,
    
    @SerializedName("deviceId")
    val deviceId: String?,
    
    @SerializedName("userId")
    val userId: String?,
    
    @SerializedName("registeredAt")
    val registeredAt: String,
    
    @SerializedName("lastActiveAt")
    val lastActiveAt: String
)

/**
 * 계좌 생성 요청 DTO
 */
data class AccountCreateRequestDto(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("accountType")
    val accountType: String, // 예: "입출금통장", "적금", "예금"
    
    @SerializedName("bankCode")
    val bankCode: String = "088", // 신한은행 고정
    
    @SerializedName("initialBalance")
    val initialBalance: Long = 0L
)