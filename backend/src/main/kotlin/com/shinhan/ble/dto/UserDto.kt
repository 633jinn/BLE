package com.shinhan.ble.dto

import com.shinhan.ble.entity.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 사용자 관련 DTO 클래스들
 */

/**
 * 로그인 요청 DTO
 */
data class LoginRequestDto(
    @field:NotBlank(message = "전화번호는 필수입니다")
    @field:Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    val phoneNumber: String,
    
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 6, max = 20, message = "비밀번호는 6-20자리여야 합니다")
    val password: String,
    
    @field:NotBlank(message = "디바이스 ID는 필수입니다")
    val deviceId: String
)

/**
 * 사용자 인증 응답 DTO
 */
data class UserAuthDto(
    val userId: String,
    val customerNumber: String,
    val userName: String,
    val phoneNumber: String,
    val email: String?,
    val accessToken: String,
    val refreshToken: String,
    val tokenExpiry: Long,
    val bleEnabled: Boolean
) {
    companion object {
        fun from(user: User, accessToken: String, refreshToken: String, tokenExpiry: Long): UserAuthDto {
            return UserAuthDto(
                userId = user.userId,
                customerNumber = user.customerNumber,
                userName = user.userName,
                phoneNumber = user.phoneNumber,
                email = user.email,
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenExpiry = tokenExpiry,
                bleEnabled = user.bleEnabled
            )
        }
    }
}

/**
 * 사용자 정보 응답 DTO
 */
data class UserInfoDto(
    val userId: String,
    val customerNumber: String,
    val userName: String,
    val phoneNumber: String,
    val email: String?,
    val isActive: Boolean,
    val bleEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(user: User): UserInfoDto {
            return UserInfoDto(
                userId = user.userId,
                customerNumber = user.customerNumber,
                userName = user.userName,
                phoneNumber = user.phoneNumber,
                email = user.email,
                isActive = user.isActive,
                bleEnabled = user.bleEnabled,
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt.toString()
            )
        }
    }
}

/**
 * 사용자 등록 요청 DTO
 */
data class UserRegistrationDto(
    @field:NotBlank(message = "고객번호는 필수입니다")
    val customerNumber: String,
    
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 2, max = 50, message = "이름은 2-50자리여야 합니다")
    val userName: String,
    
    @field:NotBlank(message = "전화번호는 필수입니다")
    @field:Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    val phoneNumber: String,
    
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String?,
    
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 6, max = 20, message = "비밀번호는 6-20자리여야 합니다")
    val password: String
)

/**
 * 사용자 설정 업데이트 DTO
 */
data class UserSettingsUpdateDto(
    val bleEnabled: Boolean? = null,
    val email: String? = null
)

/**
 * 비밀번호 변경 요청 DTO
 */
data class PasswordChangeDto(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,
    
    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 6, max = 20, message = "비밀번호는 6-20자리여야 합니다")
    val newPassword: String
)