package com.shinhan.ble.controller

import com.shinhan.ble.dto.*
import com.shinhan.ble.service.TransferService
import com.shinhan.ble.service.BleTransferCodeService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 송금 거래 REST API 컨트롤러
 */
@RestController
@RequestMapping("/transfers")
@CrossOrigin(origins = ["*"])
class TransferController(
    private val transferService: TransferService,
    private val bleTransferCodeService: BleTransferCodeService
) {
    
    /**
     * 일반 송금 처리
     */
    @PostMapping("/normal/{userId}")
    fun processTransfer(
        @PathVariable userId: String,
        @RequestBody transferRequestDto: AccountTransferRequestDto
    ): ResponseEntity<ApiResponse<TransferProcessResultDto>> {
        return try {
            val result = transferService.processTransfer(transferRequestDto, userId)
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 처리 중 오류가 발생했습니다: ${e.message}"))
        }
    }
    
    /**
     * BLE 송금 처리
     */
    @PostMapping("/ble/{userId}")
    fun processBleTransfer(
        @PathVariable userId: String,
        @RequestBody bleTransferRequestDto: BleTransferRequestDto
    ): ResponseEntity<ApiResponse<TransferProcessResultDto>> {
        return try {
            val result = transferService.processBleTransfer(bleTransferRequestDto, userId)
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 송금 처리 중 오류가 발생했습니다: ${e.message}"))
        }
    }
    
    /**
     * 송금 내역 조회
     */
    @GetMapping("/history/{userId}")
    fun getTransferHistory(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<TransferHistoryDto>>> {
        return try {
            val history = transferService.getTransferHistory(userId, page, size)
            ResponseEntity.ok(ApiResponse.success(history))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 내역 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 계좌별 송금 내역 조회
     */
    @GetMapping("/history/account/{accountId}")
    fun getAccountTransferHistory(
        @PathVariable accountId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<TransferHistoryDto>>> {
        return try {
            val history = transferService.getAccountTransferHistory(accountId, page, size)
            ResponseEntity.ok(ApiResponse.success(history))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("계좌 송금 내역 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * BLE 송금 내역 조회
     */
    @GetMapping("/history/{userId}/ble")
    fun getBleTransferHistory(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<TransferHistoryDto>>> {
        return try {
            val history = transferService.getBleTransferHistory(userId, page, size)
            ResponseEntity.ok(ApiResponse.success(history))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 송금 내역 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 거래 상세 조회
     */
    @GetMapping("/{transactionId}")
    fun getTransferDetail(@PathVariable transactionId: String): ResponseEntity<ApiResponse<TransferDetailDto>> {
        return try {
            val detail = transferService.getTransferDetail(transactionId)
            ResponseEntity.ok(ApiResponse.success(detail))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("거래 상세 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 일일 송금 총액 조회
     */
    @GetMapping("/daily-amount/{userId}")
    fun getDailyTransferAmount(@PathVariable userId: String): ResponseEntity<ApiResponse<Long>> {
        return try {
            val amount = transferService.getDailyTransferAmount(userId)
            ResponseEntity.ok(ApiResponse.success(amount))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("일일 송금 총액 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 송금 통계 조회
     */
    @GetMapping("/statistics/{userId}")
    fun getTransferStatistics(
        @PathVariable userId: String,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<ApiResponse<TransferStatisticsDto>> {
        return try {
            val statistics = transferService.getTransferStatistics(userId, year, month)
            ResponseEntity.ok(ApiResponse.success(statistics))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 통계 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 송금 취소
     */
    @PostMapping("/{transactionId}/cancel")
    fun cancelTransfer(
        @PathVariable transactionId: String,
        @RequestParam userId: String
    ): ResponseEntity<ApiResponse<TransferProcessResultDto>> {
        return try {
            val result = transferService.cancelTransfer(transactionId, userId)
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("송금 취소 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * BLE 송금코드 생성
     */
    @PostMapping("/ble-codes")
    fun generateTransferCode(@RequestBody codeGenerationDto: BleTransferCodeGenerationDto): ResponseEntity<ApiResponse<BleTransferCodeDto>> {
        return try {
            val code = bleTransferCodeService.generateTransferCode(codeGenerationDto)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(code, "BLE 송금코드가 생성되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 송금코드 생성 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * BLE 송금코드 검증
     */
    @PostMapping("/ble-codes/validate")
    fun validateTransferCode(@RequestBody request: BleTransferCodeValidationRequestDto): ResponseEntity<ApiResponse<BleTransferCodeValidationResult>> {
        return try {
            val result = bleTransferCodeService.validateTransferCode(request.transferCode)
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 송금코드 검증 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자의 활성 BLE 송금코드 조회
     */
    @GetMapping("/ble-codes/active/{userId}")
    fun getActiveTransferCode(@PathVariable userId: String): ResponseEntity<ApiResponse<BleTransferCodeDto?>> {
        return try {
            val code = bleTransferCodeService.getActiveCodeByUserId(userId)
            ResponseEntity.ok(ApiResponse.success(code))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("활성 BLE 송금코드 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * 사용자의 모든 BLE 송금코드 조회
     */
    @GetMapping("/ble-codes/user/{userId}")
    fun getUserTransferCodes(@PathVariable userId: String): ResponseEntity<ApiResponse<List<BleTransferCodeDto>>> {
        return try {
            val codes = bleTransferCodeService.getUserTransferCodes(userId)
            ResponseEntity.ok(ApiResponse.success(codes))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("사용자 BLE 송금코드 조회 중 오류가 발생했습니다"))
        }
    }
    
    /**
     * BLE 송금코드 비활성화
     */
    @DeleteMapping("/ble-codes/{codeId}")
    fun deactivateTransferCode(
        @PathVariable codeId: String,
        @RequestParam userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            bleTransferCodeService.deactivateTransferCode(codeId, userId)
            ResponseEntity.ok(ApiResponse.success("BLE 송금코드가 비활성화되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("BLE 송금코드 비활성화 중 오류가 발생했습니다"))
        }
    }
}