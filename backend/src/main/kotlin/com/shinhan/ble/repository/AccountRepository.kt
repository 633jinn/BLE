package com.shinhan.ble.repository

import com.shinhan.ble.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 계좌 정보 Repository
 */
@Repository
interface AccountRepository : JpaRepository<Account, String> {
    
    /**
     * 사용자 ID로 활성 계좌 목록 조회
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.isActive = true ORDER BY a.createdAt DESC")
    fun findActiveAccountsByUserId(@Param("userId") userId: String): List<Account>
    
    /**
     * 사용자 대표 계좌 조회
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.isActive = true AND a.isPrimary = true")
    fun findPrimaryAccountByUserId(@Param("userId") userId: String): Optional<Account>
    
    /**
     * 계좌번호로 계좌 조회
     */
    fun findByAccountNumber(accountNumber: String): Optional<Account>
    
    /**
     * 사용자 ID와 계좌 ID로 계좌 조회 (권한 체크용)
     */
    @Query("SELECT a FROM Account a WHERE a.accountId = :accountId AND a.userId = :userId AND a.isActive = true")
    fun findByAccountIdAndUserId(@Param("accountId") accountId: String, @Param("userId") userId: String): Optional<Account>
    
    /**
     * 잔액 업데이트
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance WHERE a.accountId = :accountId")
    fun updateBalance(@Param("accountId") accountId: String, @Param("newBalance") newBalance: Long): Int
    
    /**
     * 사용자의 총 계좌 수 조회
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId AND a.isActive = true")
    fun countActiveAccountsByUserId(@Param("userId") userId: String): Long
    
    /**
     * 계좌번호 존재 여부 확인
     */
    fun existsByAccountNumber(accountNumber: String): Boolean
    
    /**
     * 특정 금액 이상의 잔액을 가진 사용자 계좌 조회
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.balance >= :minBalance AND a.isActive = true")
    fun findAccountsWithMinBalance(@Param("userId") userId: String, @Param("minBalance") minBalance: Long): List<Account>
    
    /**
     * 일일 이체 한도 체크를 위한 계좌 조회
     */
    @Query("SELECT a FROM Account a WHERE a.accountId = :accountId AND a.singleTransferLimit >= :amount")
    fun findAccountWithTransferLimit(@Param("accountId") accountId: String, @Param("amount") amount: Long): Optional<Account>
    
    /**
     * 모든 계좌의 대표 설정 해제 (동일 사용자)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.isPrimary = false WHERE a.userId = :userId")
    fun clearPrimaryForUser(@Param("userId") userId: String): Int
}