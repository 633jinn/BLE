package com.shinhan.ble.service

import com.shinhan.ble.dto.*
import com.shinhan.ble.entity.BleTransferCode
import com.shinhan.ble.repository.BleTransferCodeRepository
import com.shinhan.ble.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

/**
 * BLE 송금코드 관리 서비스
 */
@Service
@Transactional
class BleTransferCodeService(
    private val bleTransferCodeRepository: BleTransferCodeRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val accountService: AccountService
) {
    
    /**
     * BLE 송금코드 생성
     */
    fun generateTransferCode(codeGenerationDto: BleTransferCodeGenerationDto): BleTransferCodeDto {
        // 사용자 정보 조회 및 검증
        val user = userRepository.findById(codeGenerationDto.userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        // 계좌 검증
        accountService.getAccountByUserAndId(codeGenerationDto.userId, codeGenerationDto.accountId)
        
        // 기존 활성 코드 비활성화
        bleTransferCodeRepository.deactivateUserActiveCodes(codeGenerationDto.userId)
        
        // BleTransferCode.generate 사용
        val bleTransferCode = BleTransferCode.generate(
            codeId = UUID.randomUUID().toString(),
            userId = codeGenerationDto.userId,
            customerNumber = user.customerNumber
        )
        
        val savedCode = bleTransferCodeRepository.save(bleTransferCode)
        return BleTransferCodeDto(
            transferCode = savedCode.transferCode,
            maskedUserName = maskUserName(user.userName),
            bankCode = "088", // 신한은행
            expiresAt = savedCode.expiresAt.toString()
        )
    }
    
    /**
     * 송금코드로 BLE 송금코드 조회
     */
    @Transactional(readOnly = true)
    fun findByTransferCode(transferCode: String): BleTransferCode? {
        return bleTransferCodeRepository.findByTransferCode(transferCode).orElse(null)
    }
    
    /**
     * 유효한 송금코드 조회
     */
    @Transactional(readOnly = true)
    fun findValidTransferCode(transferCode: String): BleTransferCode? {
        return bleTransferCodeRepository.findValidTransferCode(transferCode, LocalDateTime.now())
            .orElse(null)
    }
    
    /**
     * 사용자의 활성 송금코드 조회
     */
    @Transactional(readOnly = true)
    fun getActiveCodeByUserId(userId: String): BleTransferCodeDto? {
        return bleTransferCodeRepository.findActiveCodeByUserId(userId, LocalDateTime.now())
            .map { code -> 
                val user = userRepository.findById(userId).orElse(null)
                BleTransferCodeDto(
                    transferCode = code.transferCode,
                    maskedUserName = maskUserName(user?.userName ?: ""),
                    bankCode = "088",
                    expiresAt = code.expiresAt.toString()
                )
            }
            .orElse(null)
    }
    
    /**
     * 사용자의 모든 송금코드 조회
     */
    @Transactional(readOnly = true)
    fun getUserTransferCodes(userId: String): List<BleTransferCodeDto> {
        val user = userRepository.findById(userId).orElse(null)
        return bleTransferCodeRepository.findAllByUserId(userId)
            .map { code ->
                BleTransferCodeDto(
                    transferCode = code.transferCode,
                    maskedUserName = maskUserName(user?.userName ?: ""),
                    bankCode = "088",
                    expiresAt = code.expiresAt.toString()
                )
            }
    }
    
    /**
     * 송금코드 검증
     */
    @Transactional(readOnly = true)
    fun validateTransferCode(transferCode: String): BleTransferCodeValidationResult {
        println(transferCode)
        val code = findValidTransferCode(transferCode)

        if (code == null) {
            println("1번타자 null")
            return BleTransferCodeValidationResult(
                isValid = false,
                userName = null,
                bankName = null,
                maskedAccountNumber = null,
                expiresAt = null,
                errorMessage = "유효하지 않은 송금코드입니다"
            )
        }

        if (!code.isValid()) {
            println("2번타자 nonvalid")
            return BleTransferCodeValidationResult(
                isValid = false,
                userName = null,
                bankName = null,
                maskedAccountNumber = null,
                expiresAt = null,
                errorMessage = "만료되거나 이미 사용된 송금코드입니다"
            )
        }
        
        // 사용자 활성 상태 확인
        val user = userRepository.findById(code.userId).orElse(null)
        if (user == null || !user.isActive) {
            return BleTransferCodeValidationResult(
                isValid = false,
                userName = null,
                bankName = null,
                maskedAccountNumber = null,
                expiresAt = null,
                errorMessage = "송금코드를 생성한 사용자의 계정에 문제가 있습니다"
            )
        }
        
        return BleTransferCodeValidationResult(
            isValid = true,
            userName = maskUserName(user.userName),
            bankName = "신한은행",
            maskedAccountNumber = "",
            expiresAt = code.expiresAt.toString(),
            errorMessage = null
        )
    }
    
    /**
     * 송금코드 사용 처리
     */
    fun markTransferCodeAsUsed(transferCode: String): Boolean {
        val updatedRows = bleTransferCodeRepository.markAsUsed(transferCode, LocalDateTime.now())
        return updatedRows > 0
    }
    
    /**
     * 송금코드 비활성화
     */
    fun deactivateTransferCode(codeId: String, userId: String) {
        val code = bleTransferCodeRepository.findById(codeId)
            .orElseThrow { NoSuchElementException("송금코드를 찾을 수 없습니다") }
        
        if (code.userId != userId) {
            throw IllegalArgumentException("해당 송금코드를 비활성화할 권한이 없습니다")
        }
        
        val deactivatedCode = code.copy(isActive = false)
        bleTransferCodeRepository.save(deactivatedCode)
    }
    
    /**
     * 만료된 송금코드 정리
     */
    @Transactional
    fun cleanupExpiredCodes(): Int {
        return bleTransferCodeRepository.deactivateExpiredCodes(LocalDateTime.now())
    }
    
    /**
     * 사용자의 활성 송금코드 개수 조회
     */
    @Transactional(readOnly = true)
    fun getActiveCodeCount(userId: String): Long {
        return bleTransferCodeRepository.countActiveCodesByUserId(userId, LocalDateTime.now())
    }
    
    /**
     * BLE 송금코드를 이용한 송금 처리
     */
    fun processBleTransferWithCode(bleTransferRequestDto: BleTransferRequestDto): TransferResultDto {
        // 송금코드 검증
        val validation = validateTransferCode(bleTransferRequestDto.toTransferCode)
        if (!validation.isValid) {
            return TransferResultDto(
                transferId = "",
                transactionId = "",
                fromAccountNumber = "",
                toAccountNumber = "",
                fromUserName = "",
                toUserName = "",
                amount = 0L,
                fee = 0L,
                status = "FAILED",
                transferType = "BLE",
                memo = validation.errorMessage ?: "송금코드 검증에 실패했습니다",
                createdAt = LocalDateTime.now().toString(),
                completedAt = null,
                errorMessage = validation.errorMessage ?: "송금코드 검증에 실패했습니다"
            )
        }
        
        val transferCode = findValidTransferCode(bleTransferRequestDto.toTransferCode)!!
        
        // 송금코드 사용 처리
        markTransferCodeAsUsed(bleTransferRequestDto.toTransferCode)
        
        return TransferResultDto(
            transferId = transferCode.codeId,
            transactionId = transferCode.codeId,
            fromAccountNumber = "",
            toAccountNumber = "",
            fromUserName = "",
            toUserName = "",
            amount = bleTransferRequestDto.amount,
            fee = 0L,
            status = "SUCCESS",
            transferType = "BLE",
            memo = "BLE 송금코드 검증이 완료되었습니다",
            createdAt = LocalDateTime.now().toString(),
            completedAt = LocalDateTime.now().toString(),
            errorMessage = null
        )
    }
    
    /**
     * 특정 기간 내 생성된 송금코드 조회
     */
    @Transactional(readOnly = true)
    fun getCodesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<BleTransferCodeDto> {
        return bleTransferCodeRepository.findCodesByDateRange(startDate, endDate)
            .map { code ->
                val user = userRepository.findById(code.userId).orElse(null)
                BleTransferCodeDto(
                    transferCode = code.transferCode,
                    maskedUserName = maskUserName(user?.userName ?: ""),
                    bankCode = "088",
                    expiresAt = code.expiresAt.toString()
                )
            }
    }
    
    /**
     * 고유한 송금코드 생성
     */
    private fun generateUniqueTransferCode(): String {
        var attempts = 0
        val maxAttempts = 100
        
        while (attempts < maxAttempts) {
            val code = generateRandomCode()
            if (!bleTransferCodeRepository.existsByTransferCode(code)) {
                return code
            }
            attempts++
        }
        
        throw RuntimeException("고유한 송금코드 생성에 실패했습니다")
    }
    
    /**
     * 랜덤 송금코드 생성 (6자리 숫자)
     */
    private fun generateRandomCode(): String {
        val random = Random.Default
        return (100000..999999).random(random).toString()
    }
    
    /**
     * 오래된 송금코드 삭제 (데이터 정리용)
     */
    @Transactional
    fun deleteOldCodes(daysOld: Long = 30): Int {
        val cutoffDate = LocalDateTime.now().minusDays(daysOld)
        return bleTransferCodeRepository.deleteExpiredCodes(cutoffDate)
    }
    
    /**
     * 사용자 이름 마스킹
     */
    private fun maskUserName(userName: String): String {
        if (userName.isEmpty()) return ""
        return when {
            userName.length <= 2 -> userName
            userName.length == 3 -> "${userName[0]}*${userName[2]}"
            else -> "${userName[0]}${"*".repeat(userName.length - 2)}${userName.last()}"
        }
    }
}