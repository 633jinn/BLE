package com.shinhan.ble.service

import com.shinhan.ble.dto.*
import com.shinhan.ble.entity.Account
import com.shinhan.ble.entity.User
import com.shinhan.ble.repository.AccountRepository
import com.shinhan.ble.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 계좌 관리 서비스
 */
@Service
@Transactional
class AccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {
    
    /**
     * 사용자의 활성 계좌 목록 조회
     */
    @Transactional(readOnly = true)
    fun getUserAccounts(userId: String): List<AccountInfoDto> {
        return accountRepository.findActiveAccountsByUserId(userId)
            .map { AccountInfoDto.from(it) }
    }

    /**
     * 대표 계좌 조회
     */
    @Transactional(readOnly = true)
    fun getPrimaryAccount(userId: String): AccountInfoDto? {
        return accountRepository.findPrimaryAccountByUserId(userId)
            .map { AccountInfoDto.from(it) }
            .orElse(null)
    }
    
    /**
     * 계좌 정보 조회
     */
    @Transactional(readOnly = true)
    fun getAccountInfo(accountId: String): AccountInfoDto {
        val account = accountRepository.findById(accountId)
            .orElseThrow { NoSuchElementException("계좌를 찾을 수 없습니다") }
        return AccountInfoDto.from(account)
    }
    
    /**
     * 사용자와 계좌 ID로 계좌 조회 (권한 체크)
     */
    @Transactional(readOnly = true)
    fun getAccountByUserAndId(userId: String, accountId: String): Account {
        return accountRepository.findByAccountIdAndUserId(accountId, userId)
            .orElseThrow { IllegalArgumentException("해당 계좌에 접근할 권한이 없습니다") }
    }
    
    /**
     * 계좌번호로 계좌 조회
     */
    @Transactional(readOnly = true)
    fun findByAccountNumber(accountNumber: String): Account? {
        return accountRepository.findByAccountNumber(accountNumber).orElse(null)
    }
    
    /**
     * 계좌 잔액 조회
     */
    @Transactional(readOnly = true)
    fun getAccountBalance(userId: String, accountId: String): Long {
        val account = getAccountByUserAndId(userId, accountId)
        return account.balance
    }
    
    /**
     * 계좌 잔액 업데이트
     */
    fun updateBalance(accountId: String, newBalance: Long): Boolean {
        val updatedRows = accountRepository.updateBalance(accountId, newBalance)
        return updatedRows > 0
    }
    
    /**
     * 송금 가능 여부 확인
     */
    @Transactional(readOnly = true)
    fun canTransfer(userId: String, accountId: String, amount: Long): TransferValidationResult {
        val account = getAccountByUserAndId(userId, accountId)
        
        // 잔액 확인
        if (account.balance < amount) {
            return TransferValidationResult(
                isValid = false,
                availableBalance = account.balance,
                dailyLimitRemaining = account.dailyTransferLimit,
                singleLimitExceeded = false,
                errorMessage = "잔액이 부족합니다"
            )
        }
        
        // 송금 한도 확인
        if (amount > account.singleTransferLimit) {
            return TransferValidationResult(
                isValid = false,
                availableBalance = account.balance,
                dailyLimitRemaining = account.dailyTransferLimit,
                singleLimitExceeded = true,
                errorMessage = "1회 송금 한도를 초과했습니다"
            )
        }
        
        if (amount > account.dailyTransferLimit) {
            return TransferValidationResult(
                isValid = false,
                availableBalance = account.balance,
                dailyLimitRemaining = account.dailyTransferLimit,
                singleLimitExceeded = false,
                errorMessage = "일일 송금 한도를 초과했습니다"
            )
        }
        
        return TransferValidationResult(
            isValid = true,
            availableBalance = account.balance,
            dailyLimitRemaining = account.dailyTransferLimit,
            singleLimitExceeded = false,
            errorMessage = null
        )
    }
    
    /**
     * 특정 금액 이상의 잔액을 가진 계좌 조회
     */
    @Transactional(readOnly = true)
    fun getAccountsWithMinBalance(userId: String, minBalance: Long): List<AccountInfoDto> {
        return accountRepository.findAccountsWithMinBalance(userId, minBalance)
            .map { AccountInfoDto.from(it) }
    }
    
    /**
     * 계좌 생성
     */
    fun createAccount(accountCreationDto: AccountCreationDto): AccountDto {
        // 사용자 존재 여부 확인 및 자동 생성
        ensureUserExists(accountCreationDto.userId)
        
        // 새 계좌번호 생성 (간단한 예시)
        val accountNumber = generateAccountNumber(accountCreationDto.bankCode)
        
        // 계좌번호 중복 체크
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw IllegalArgumentException("계좌 생성 중 오류가 발생했습니다")
        }
        
        // 첫 계좌 여부 판단
        val hasActiveAccounts = accountRepository.countActiveAccountsByUserId(accountCreationDto.userId) > 0

        val account = Account(
            accountId = UUID.randomUUID().toString(),
            userId = accountCreationDto.userId,
            accountNumber = accountNumber,
            accountType = accountCreationDto.accountType,
            bankName = getBankName(accountCreationDto.bankCode),
            bankCode = accountCreationDto.bankCode,
            balance = accountCreationDto.initialBalance,
            singleTransferLimit = 5000000L, // 기본값 500만원
            dailyTransferLimit = 10000000L, // 기본값 1000만원
            currency = "KRW",
            isPrimary = !hasActiveAccounts // 첫 계좌면 자동 대표 지정
        )
        
        val savedAccount = accountRepository.save(account)
        return AccountInfoDto.from(savedAccount)
    }

    /**
     * 대표 계좌 설정 (사용자당 1개)
     */
    fun setPrimaryAccount(userId: String, accountId: String): AccountInfoDto {
        // 권한 검증 포함 계좌 조회
        val account = getAccountByUserAndId(userId, accountId)
        // 동일 사용자 다른 계좌의 대표 해제
        accountRepository.clearPrimaryForUser(userId)
        val updated = account.copy(isPrimary = true)
        val saved = accountRepository.save(updated)
        return AccountInfoDto.from(saved)
    }
    
    /**
     * 계좌 비활성화
     */
    fun deactivateAccount(userId: String, accountId: String) {
        val account = getAccountByUserAndId(userId, accountId)
        val deactivatedAccount = account.copy(isActive = false)
        accountRepository.save(deactivatedAccount)
    }
    
    /**
     * 계좌 한도 업데이트
     */
    fun updateTransferLimits(
        userId: String, 
        accountId: String, 
        singleLimit: Long?, 
        dailyLimit: Long?
    ): AccountInfoDto {
        val account = getAccountByUserAndId(userId, accountId)
        
        val updatedAccount = account.copy(
            singleTransferLimit = singleLimit ?: account.singleTransferLimit,
            dailyTransferLimit = dailyLimit ?: account.dailyTransferLimit
        )
        
        val savedAccount = accountRepository.save(updatedAccount)
        return AccountInfoDto.from(savedAccount)
    }
    
    /**
     * 사용자의 계좌 수 조회
     */
    @Transactional(readOnly = true)
    fun getAccountCount(userId: String): Long {
        return accountRepository.countActiveAccountsByUserId(userId)
    }
    
    /**
     * 계좌번호 생성 (간단한 예시)
     */
    private fun generateAccountNumber(bankCode: String): String {
        val timestamp = System.currentTimeMillis()
        return "$bankCode${timestamp % 100000000}"
    }
    
    /**
     * 은행 코드로 은행명 반환
     */
    private fun getBankName(bankCode: String): String {
        return when (bankCode) {
            "088" -> "신한은행"
            "004" -> "국민은행"
            "081" -> "하나은행"
            "020" -> "우리은행"
            "090" -> "카카오뱅크"
            "092" -> "토스뱅크"
            else -> "기타은행"
        }
    }
    
    /**
     * 사용자 존재 여부 확인 및 자동 생성
     */
    private fun ensureUserExists(userId: String) {
        if (!userRepository.existsById(userId)) {
            val timestamp = System.currentTimeMillis()
            val newUser = User(
                userId = userId,
                customerNumber = "CUST${userId.substring(0, 8)}${timestamp % 10000}", // 고객번호 생성
                userName = "자동생성사용자",
                phoneNumber = "010${userId.substring(0, 4)}${timestamp % 10000}", // 임시 전화번호 생성
                email = null,
                passwordHash = "temp_hash_${userId.substring(0, 8)}", // 임시 해시
                isActive = true,
                bleEnabled = true
            )
            userRepository.save(newUser)
        }
    }
}