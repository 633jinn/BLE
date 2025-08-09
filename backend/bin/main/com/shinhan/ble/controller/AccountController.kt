package com.shinhan.ble.controller

import com.shinhan.ble.dto.*
import com.shinhan.ble.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 계좌 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/accounts")
@CrossOrigin(origins = ["*"])
class AccountController(
    private val accountService: AccountService
) {
    
    /**
     * 사용자의 계좌 목록 조회
     */
    @GetMapping("/user/{userId}")
    fun getUserAccounts(@PathVariable userId: String): ResponseEntity<ApiResponse<List<AccountDto>>> {
        return try {
            val accounts = accountService.getUserAccounts(userId)
            ResponseEntity.ok(ApiResponse.success(accounts))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 목록 조회 중 오류가 발생했습니다"))
        }
    }

    /**
     * 대표 계좌 조회
     */
    @GetMapping("/user/{userId}/primary")
    fun getPrimaryAccount(@PathVariable userId: String): ResponseEntity<ApiResponse<AccountDto?>> {
        return try {
            val primary = accountService.getPrimaryAccount(userId)
            ResponseEntity.ok(ApiResponse.success(primary))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("대표 계좌 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 계좌 정보 조회
     */
    @GetMapping("/{accountId}")
    fun getAccountInfo(@PathVariable accountId: String): ResponseEntity<ApiResponse<AccountInfoDto>> {
        return try {
            val account = accountService.getAccountInfo(accountId)
            ResponseEntity.ok(ApiResponse.success(account))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 정보 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자별 계좌 잔액 조회
     */
    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @PathVariable accountId: String,
        @RequestParam userId: String
    ): ResponseEntity<ApiResponse<Long>> {
        return try {
            val balance = accountService.getAccountBalance(userId, accountId)
            ResponseEntity.ok(ApiResponse.success(balance))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 잔액 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 송금 가능 여부 확인
     */
    @PostMapping("/{accountId}/transfer-validation")
    fun validateTransfer(
        @PathVariable accountId: String,
        @RequestBody validationRequest: TransferValidationRequest
    ): ResponseEntity<ApiResponse<TransferValidationResult>> {
        return try {
            val result = accountService.canTransfer(
                validationRequest.userId,
                accountId,
                validationRequest.amount
            )
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 유효성 검증 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 최소 잔액 이상의 계좌 조회
     */
    @GetMapping("/user/{userId}/min-balance/{minBalance}")
    fun getAccountsWithMinBalance(
        @PathVariable userId: String,
        @PathVariable minBalance: Long
    ): ResponseEntity<ApiResponse<List<AccountInfoDto>>> {
        return try {
            val accounts = accountService.getAccountsWithMinBalance(userId, minBalance)
            ResponseEntity.ok(ApiResponse.success(accounts))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 새 계좌 생성
     */
    @PostMapping
    fun createAccount(@RequestBody accountCreationDto: AccountCreationDto): ResponseEntity<ApiResponse<AccountInfoDto>> {
        return try {
            val account = accountService.createAccount(accountCreationDto)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "계좌가 생성되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 생성 중 오류가 발생했습니다"))
        }
    }

    /**
     * 대표 계좌 설정
     */
    @PutMapping("/user/{userId}/primary/{accountId}")
    fun setPrimaryAccount(
        @PathVariable userId: String,
        @PathVariable accountId: String
    ): ResponseEntity<ApiResponse<AccountInfoDto>> {
        return try {
            val updated = accountService.setPrimaryAccount(userId, accountId)
            ResponseEntity.ok(ApiResponse.success(updated, "대표 계좌가 설정되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("대표 계좌 설정 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 계좌 비활성화
     */
    @DeleteMapping("/{accountId}")
    fun deactivateAccount(
        @PathVariable accountId: String,
        @RequestParam userId: String
    ): ResponseEntity<ShinhanApiResponse<Unit>> {
        return try {
            accountService.deactivateAccount(userId, accountId)
            ResponseEntity.ok(ApiResponse.success("계좌가 비활성화되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 비활성화 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 계좌 송금 한도 업데이트
     */
    @PutMapping("/{accountId}/limits")
    fun updateTransferLimits(
        @PathVariable accountId: String,
        @RequestBody limitsUpdateDto: TransferLimitsUpdateDto
    ): ResponseEntity<ApiResponse<AccountInfoDto>> {
        return try {
            val updatedAccount = accountService.updateTransferLimits(
                limitsUpdateDto.userId,
                accountId,
                limitsUpdateDto.singleLimit,
                limitsUpdateDto.dailyLimit
            )
            ResponseEntity.ok(ApiResponse.success(updatedAccount, "송금 한도가 업데이트되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 한도 업데이트 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자의 계좌 수 조회
     */
    @GetMapping("/user/{userId}/count")
    fun getAccountCount(@PathVariable userId: String): ResponseEntity<ApiResponse<Long>> {
        return try {
            val count = accountService.getAccountCount(userId)
            ResponseEntity.ok(ApiResponse.success(count))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 수 조회 중 오류가 발생했습니다"))
        }
    }
}