/**
 * 신한은행 BLE 송금 서비스 공통 모델
 * 안드로이드 앱과 백엔드 서버에서 공통으로 사용하는 데이터 모델들
 */

/**
 * API 공통 응답 형식
 */
data class ShinhanApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
    val timestamp: String
)

/**
 * 계좌 정보
 */
data class AccountInfo(
    val accountId: String,
    val accountNumber: String,
    val accountType: String,
    val bankName: String,
    val bankCode: String,
    val balance: Long,
    val currency: String = "KRW",
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 송금 요청
 */
data class TransferRequest(
    val fromAccountId: String,
    val toTransferCode: String,
    val amount: Long,
    val memo: String? = null,
    val authToken: String
)

/**
 * 송금 결과
 */
data class TransferResult(
    val transactionId: String,
    val fromAccountNumber: String,
    val toAccountNumber: String,
    val toCustomerName: String,
    val amount: Long,
    val fee: Long,
    val status: String, // SUCCESS, FAILED, PENDING
    val processedAt: String,
    val memo: String?
)

/**
 * 사용자 인증 정보
 */
data class UserAuth(
    val customerId: String,
    val customerName: String,
    val phoneNumber: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenExpiry: Long
)

/**
 * 로그인 요청
 */
data class LoginRequest(
    val phoneNumber: String,
    val password: String,
    val deviceId: String
)

/**
 * 송금코드 검증 요청
 */
data class TransferCodeValidation(
    val transferCode: String
)

/**
 * 송금코드 검증 결과
 */
data class TransferCodeValidationResult(
    val isValid: Boolean,
    val customerName: String?,
    val bankName: String?,
    val expiresAt: String?
)

/**
 * BLE 송금코드 정보
 */
data class BleTransferCode(
    val transferCode: String,
    val customerName: String,
    val bankCode: String = "088",
    val expiresAt: String
)

/**
 * 에러 응답
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)