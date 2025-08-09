package com.shinhan.ble.dto

import java.time.LocalDateTime

// 기존 코드와의 호환성을 위한 타입 별칭
typealias ApiResponse<T> = ShinhanApiResponse<T>

/**
 * API 공통 응답 형식
 */
data class ShinhanApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
    val timestamp: String = LocalDateTime.now().toString()
) {
    companion object {
        /**
         * 성공 응답 생성
         */
        fun <T> success(data: T, message: String = "성공"): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = true,
                code = "SUCCESS",
                message = message,
                data = data
            )
        }
        
        /**
         * 성공 응답 생성 (데이터 없음)
         */
        fun success(message: String = "성공"): ShinhanApiResponse<Unit> {
            return ShinhanApiResponse(
                success = true,
                code = "SUCCESS",
                message = message,
                data = Unit
            )
        }
        
        /**
         * 실패 응답 생성
         */
        fun <T> failure(code: String, message: String): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = code,
                message = message,
                data = null
            )
        }
        
        /**
         * 에러 응답 생성
         */
        fun <T> error(message: String): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = "ERROR",
                message = message,
                data = null
            )
        }
        
        /**
         * 유효성 검사 실패 응답
         */
        fun <T> validationError(message: String): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = "VALIDATION_ERROR",
                message = message,
                data = null
            )
        }
        
        /**
         * 인증 실패 응답
         */
        fun <T> unauthorized(message: String = "인증이 필요합니다"): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = "UNAUTHORIZED",
                message = message,
                data = null
            )
        }
        
        /**
         * 권한 없음 응답
         */
        fun <T> forbidden(message: String = "권한이 없습니다"): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = "FORBIDDEN",
                message = message,
                data = null
            )
        }
        
        /**
         * 리소스 없음 응답
         */
        fun <T> notFound(message: String = "리소스를 찾을 수 없습니다"): ShinhanApiResponse<T> {
            return ShinhanApiResponse(
                success = false,
                code = "NOT_FOUND",
                message = message,
                data = null
            )
        }
    }
}

/**
 * 페이징 응답을 위한 래퍼 클래스
 */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)