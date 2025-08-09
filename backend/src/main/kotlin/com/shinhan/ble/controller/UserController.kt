package com.shinhan.ble.controller

import com.shinhan.ble.dto.*
import com.shinhan.ble.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userService: UserService
) {
    
    /**
     * 사용자 등록
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody registrationDto: UserRegistrationDto): ResponseEntity<ApiResponse<UserInfoDto>> {
        return try {
            val userInfo = userService.registerUser(registrationDto)
            ResponseEntity.ok(ApiResponse.success(userInfo, "사용자 등록이 완료되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 등록 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자 정보 조회
     */
    @GetMapping("/{userId}")
    fun getUserInfo(@PathVariable userId: String): ResponseEntity<ApiResponse<UserInfoDto>> {
        return try {
            val userInfo = userService.getUserInfo(userId)
            ResponseEntity.ok(ApiResponse.success(userInfo))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 정보 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 전화번호로 사용자 조회
     */
    @GetMapping("/phone/{phoneNumber}")
    fun getUserByPhoneNumber(@PathVariable phoneNumber: String): ResponseEntity<ApiResponse<UserInfoDto>> {
        return try {
            val user = userService.findByPhoneNumber(phoneNumber)
            if (user != null) {
                val userInfo = UserInfoDto.from(user)
                ResponseEntity.ok(ApiResponse.success(userInfo))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자 설정 업데이트
     */
    @PutMapping("/{userId}/settings")
    fun updateUserSettings(
        @PathVariable userId: String,
        @RequestBody settingsDto: UserSettingsUpdateDto
    ): ResponseEntity<ApiResponse<UserInfoDto>> {
        return try {
            val updatedUser = userService.updateUserSettings(userId, settingsDto)
            ResponseEntity.ok(ApiResponse.success(updatedUser, "사용자 설정이 업데이트되었습니다"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 설정 업데이트 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 비밀번호 변경
     */
    @PutMapping("/{userId}/password")
    fun changePassword(
        @PathVariable userId: String,
        @RequestBody passwordChangeDto: PasswordChangeDto
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            userService.changePassword(userId, passwordChangeDto.currentPassword, passwordChangeDto.newPassword)
            ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자 비활성화
     */
    @DeleteMapping("/{userId}")
    fun deactivateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<Unit>> {
        return try {
            userService.deactivateUser(userId)
            ResponseEntity.ok(ApiResponse.success("사용자가 비활성화되었습니다"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 비활성화 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * BLE 활성화된 사용자 목록 조회
     */
    @GetMapping("/ble-enabled")
    fun getBleEnabledUsers(): ResponseEntity<ApiResponse<List<UserInfoDto>>> {
        return try {
            val users = userService.getBleEnabledUsers()
            ResponseEntity.ok(ApiResponse.success(users))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 사용자 목록 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 활성 사용자 여부 확인
     */
    @GetMapping("/active-check/{phoneNumber}")
    fun checkActiveUser(@PathVariable phoneNumber: String): ResponseEntity<ApiResponse<Boolean>> {
        return try {
            val isActive = userService.isActiveUser(phoneNumber)
            ResponseEntity.ok(ApiResponse.success(isActive))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 활성 상태 확인 중 오류가 발생했습니다"))
        }
    }
}