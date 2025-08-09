package com.shinhan.ble.service

import com.shinhan.ble.dto.*
import com.shinhan.ble.entity.Transfer
import com.shinhan.ble.entity.TransferStatus
import com.shinhan.ble.entity.TransferType
import com.shinhan.ble.repository.TransferRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 송금 거래 처리 서비스
 */
@Service
@Transactional
class TransferService(
    private val transferRepository: TransferRepository,
    private val accountService: AccountService,
    private val userService: UserService,
    private val bleTransferCodeService: BleTransferCodeService
) {
    
    /**
     * 일반 송금 처리
     */
    fun processTransfer(transferRequestDto: AccountTransferRequestDto, userId: String): TransferProcessResultDto {
        try {
            // 송금 유효성 검증
            val validation = validateTransfer(transferRequestDto, userId)
            if (!validation.isValid) {
                return TransferProcessResultDto(
                    success = false,
                    transactionId = null,
                    message = validation.errorMessage ?: "송금할 수 없습니다"
                )
            }
            
            // 송금 거래 생성
            val transfer = createTransfer(transferRequestDto, userId, TransferType.ACCOUNT)
            
            // 잔액 업데이트
            val fromAccount = accountService.getAccountByUserAndId(
                userId, 
                transferRequestDto.fromAccountId
            )
            val toAccount = accountService.findByAccountNumber(transferRequestDto.toAccountNumber)
                ?: throw IllegalArgumentException("수취 계좌를 찾을 수 없습니다")
            
            // 출금 계좌 잔액 차감
            val newFromBalance = fromAccount.balance - transferRequestDto.amount
            accountService.updateBalance(fromAccount.accountId, newFromBalance)
            
            // 입금 계좌 잔액 증가
            val newToBalance = toAccount.balance + transferRequestDto.amount
            accountService.updateBalance(toAccount.accountId, newToBalance)
            
            // 송금 완료 처리
            val completedTransfer = transfer.copy(
                status = TransferStatus.COMPLETED,
                completedAt = LocalDateTime.now()
            )
            transferRepository.save(completedTransfer)
            
            return TransferProcessResultDto(
                success = true,
                transactionId = completedTransfer.transactionId,
                message = "송금이 완료되었습니다"
            )
            
        } catch (e: Exception) {
            // 실패 처리
            val failedTransfer = createTransfer(transferRequestDto, userId, TransferType.ACCOUNT).copy(
                status = TransferStatus.FAILED,
                errorMessage = e.message
            )
            transferRepository.save(failedTransfer)
            
            return TransferProcessResultDto(
                success = false,
                transactionId = failedTransfer.transactionId,
                message = e.message ?: "송금 처리 중 오류가 발생했습니다"
            )
        }
    }
    
    /**
     * BLE 송금 처리
     */
    fun processBleTransfer(bleTransferRequestDto: BleTransferRequestDto, userId: String): TransferProcessResultDto {
        try {
            // 1. BLE 송금코드 검증
            val codeValidation = bleTransferCodeService.validateTransferCode(bleTransferRequestDto.toTransferCode)
            if (!codeValidation.isValid) {
                return TransferProcessResultDto(
                    success = false,
                    transactionId = null,
                    message = codeValidation.errorMessage ?: "유효하지 않은 송금코드입니다"
                )
            }
            
            // 2. 송금코드로부터 수취인 정보 조회
            val transferCode = bleTransferCodeService.findValidTransferCode(bleTransferRequestDto.toTransferCode)
                ?: throw IllegalArgumentException("유효한 송금코드를 찾을 수 없습니다")
            
            // 3. 수취인의 기본 계좌 조회 (첫 번째 활성 계좌 사용)
            val toUserAccounts = accountService.getUserAccounts(transferCode.userId)
            val toAccount = toUserAccounts.firstOrNull()
                ?: throw IllegalArgumentException("수취인의 활성 계좌를 찾을 수 없습니다")
            
            // 4. 수취인 계좌의 실제 Account 엔티티 조회
            val toAccountEntity = accountService.getAccountByUserAndId(transferCode.userId, toAccount.accountId)
            
            // 5. 송금 요청을 일반 송금 형태로 변환
            val transferRequest = AccountTransferRequestDto(
                fromAccountId = bleTransferRequestDto.fromAccountId,
                toAccountNumber = toAccountEntity.accountNumber,
                amount = bleTransferRequestDto.amount,
                memo = bleTransferRequestDto.memo ?: "BLE 송금",
                authToken = bleTransferRequestDto.authToken
            )
            
            // 6. 송금 유효성 검증
            val validation = validateTransfer(transferRequest, userId)
            if (!validation.isValid) {
                return TransferProcessResultDto(
                    success = false,
                    transactionId = null,
                    message = validation.errorMessage ?: "BLE 송금할 수 없습니다"
                )
            }
            
            // 7. BLE 송금 거래 생성
            val transfer = createBleTransfer(transferRequest, userId, bleTransferRequestDto.toTransferCode)
            
            // 8. 잔액 업데이트
            val fromAccount = accountService.getAccountByUserAndId(userId, transferRequest.fromAccountId)
            
            val newFromBalance = fromAccount.balance - transferRequest.amount
            accountService.updateBalance(fromAccount.accountId, newFromBalance)
            
            val newToBalance = toAccountEntity.balance + transferRequest.amount
            accountService.updateBalance(toAccountEntity.accountId, newToBalance)
            
            // 9. 송금코드 사용 처리
            bleTransferCodeService.markTransferCodeAsUsed(bleTransferRequestDto.toTransferCode)
            
            // 10. BLE 송금 완료 처리
            val completedTransfer = transfer.copy(
                status = TransferStatus.COMPLETED,
                completedAt = LocalDateTime.now()
            )
            transferRepository.save(completedTransfer)
            
            return TransferProcessResultDto(
                success = true,
                transactionId = completedTransfer.transactionId,
                message = "BLE 송금이 완료되었습니다"
            )
            
        } catch (e: Exception) {
            return TransferProcessResultDto(
                success = false,
                transactionId = null,
                message = e.message ?: "BLE 송금 처리 중 오류가 발생했습니다"
            )
        }
    }
    
    /**
     * 송금 내역 조회
     */
    @Transactional(readOnly = true)
    fun getTransferHistory(userId: String, page: Int = 0, size: Int = 20): Page<TransferHistoryDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        return transferRepository.findTransferHistoryByUserId(userId, pageable)
            .map { TransferHistoryDto.from(it, userId) }
    }
    
    /**
     * 특정 계좌의 송금 내역 조회
     */
    @Transactional(readOnly = true)
    fun getAccountTransferHistory(accountId: String, page: Int = 0, size: Int = 20): Page<TransferHistoryDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        // TODO: 계좌별 내역 조회시 userId 파라미터 추가 필요
        val userId = "" // 임시: 사용자 ID 확인 로직 필요
        return transferRepository.findTransferHistoryByAccountId(accountId, pageable)
            .map { TransferHistoryDto.from(it, userId) }
    }
    
    /**
     * BLE 송금 내역 조회
     */
    @Transactional(readOnly = true)
    fun getBleTransferHistory(userId: String, page: Int = 0, size: Int = 20): Page<TransferHistoryDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        return transferRepository.findBleTransfersByUserId(userId, pageable)
            .map { TransferHistoryDto.from(it, userId) }
    }
    
    /**
     * 거래 상세 조회
     */
    @Transactional(readOnly = true)
    fun getTransferDetail(transactionId: String): TransferDetailDto {
        val transfer = transferRepository.findByTransactionId(transactionId)
            .orElseThrow { NoSuchElementException("거래 내역을 찾을 수 없습니다") }
        return TransferDetailDto.from(transfer)
    }
    
    /**
     * 일일 송금 총액 조회
     */
    @Transactional(readOnly = true)
    fun getDailyTransferAmount(userId: String, date: LocalDateTime = LocalDateTime.now()): Long {
        return transferRepository.getDailyTransferAmount(userId, date)
    }
    
    /**
     * 송금 통계 조회
     */
    @Transactional(readOnly = true)
    fun getTransferStatistics(userId: String, year: Int, month: Int): TransferStatisticsDto {
        val transferCount = transferRepository.getMonthlyTransferCount(userId, year, month)
        val transferAmount = transferRepository.getMonthlyTransferAmount(userId, year, month)
        
        return TransferStatisticsDto(
            userId = userId,
            year = year,
            month = month,
            totalCount = transferCount,
            totalAmount = transferAmount
        )
    }
    
    /**
     * 송금 취소 (특정 조건 하에서만)
     */
    fun cancelTransfer(transactionId: String, userId: String): TransferProcessResultDto {
        val transfer = transferRepository.findByTransactionId(transactionId)
            .orElseThrow { NoSuchElementException("거래 내역을 찾을 수 없습니다") }
        
        // 권한 확인
        if (transfer.fromUserId != userId) {
            throw IllegalArgumentException("해당 거래를 취소할 권한이 없습니다")
        }
        
        // 취소 가능 상태 확인
        if (transfer.status != TransferStatus.PENDING) {
            return TransferProcessResultDto(
                success = false,
                transactionId = transactionId,
                message = "이미 처리된 거래는 취소할 수 없습니다"
            )
        }
        
        // 취소 처리
        val cancelledTransfer = transfer.copy(
            status = TransferStatus.CANCELLED,
            errorMessage = "사용자 요청에 의한 취소"
        )
        transferRepository.save(cancelledTransfer)
        
        return TransferProcessResultDto(
            success = true,
            transactionId = transactionId,
            message = "송금이 취소되었습니다"
        )
    }
    
    /**
     * 송금 유효성 검증
     */
    private fun validateTransfer(transferRequestDto: AccountTransferRequestDto, userId: String): TransferValidationResult {
        // 송금 금액 검증
        if (transferRequestDto.amount <= 0) {
            return TransferValidationResult(
                isValid = false,
                availableBalance = 0L,
                dailyLimitRemaining = 0L,
                singleLimitExceeded = false,
                errorMessage = "송금 금액이 올바르지 않습니다"
            )
        }
        
        // 계좌 송금 가능 여부 확인
        return accountService.canTransfer(
            userId,
            transferRequestDto.fromAccountId,
            transferRequestDto.amount
        )
    }
    
    /**
     * 송금 거래 생성
     */
    private fun createTransfer(transferRequestDto: AccountTransferRequestDto, userId: String, transferType: TransferType): Transfer {
        val toAccount = accountService.findByAccountNumber(transferRequestDto.toAccountNumber)
            ?: throw IllegalArgumentException("수취 계좌를 찾을 수 없습니다")
        
        return Transfer(
            transferId = generateTransferId(),
            transactionId = generateTransactionId(),
            fromUserId = userId,
            fromAccountId = transferRequestDto.fromAccountId,
            toUserId = toAccount.userId,
            toAccountId = toAccount.accountId,
            amount = transferRequestDto.amount,
            memo = transferRequestDto.memo,
            transferType = transferType,
            status = TransferStatus.PENDING
        )
    }
    
    /**
     * BLE 송금 거래 생성
     */
    private fun createBleTransfer(transferRequestDto: AccountTransferRequestDto, userId: String, transferCode: String): Transfer {
        val toAccount = accountService.findByAccountNumber(transferRequestDto.toAccountNumber)
            ?: throw IllegalArgumentException("수취 계좌를 찾을 수 없습니다")
        
        return Transfer(
            transferId = generateTransferId(),
            transactionId = generateTransactionId(),
            fromUserId = userId,
            fromAccountId = transferRequestDto.fromAccountId,
            toUserId = toAccount.userId,
            toAccountId = toAccount.accountId,
            amount = transferRequestDto.amount,
            memo = transferRequestDto.memo,
            transferType = TransferType.BLE,
            status = TransferStatus.PENDING,
            transferCode = transferCode
        )
    }
    
    /**
     * 송금 ID 생성
     */
    private fun generateTransferId(): String {
        return UUID.randomUUID().toString()
    }
    
    /**
     * 거래 ID 생성
     */
    private fun generateTransactionId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "TX${timestamp}${random}"
    }
}