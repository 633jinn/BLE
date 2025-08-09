package com.shinhan.ble.repository

import com.shinhan.ble.entity.Transfer
import com.shinhan.ble.entity.TransferStatus
import com.shinhan.ble.entity.TransferType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 송금 거래 Repository
 */
@Repository
interface TransferRepository : JpaRepository<Transfer, String> {
    
    /**
     * 거래 ID로 송금 내역 조회
     */
    fun findByTransactionId(transactionId: String): Optional<Transfer>
    
    /**
     * 사용자의 송금 내역 조회 (송금 + 입금)
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE (t.fromUserId = :userId OR t.toUserId = :userId) 
        ORDER BY t.createdAt DESC
    """)
    fun findTransferHistoryByUserId(@Param("userId") userId: String, pageable: Pageable): Page<Transfer>
    
    /**
     * 특정 계좌의 송금 내역 조회
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) 
        ORDER BY t.createdAt DESC
    """)
    fun findTransferHistoryByAccountId(@Param("accountId") accountId: String, pageable: Pageable): Page<Transfer>
    
    /**
     * 사용자의 특정 상태 송금 내역 조회
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE (t.fromUserId = :userId OR t.toUserId = :userId) 
        AND t.status = :status 
        ORDER BY t.createdAt DESC
    """)
    fun findTransferHistoryByUserIdAndStatus(
        @Param("userId") userId: String, 
        @Param("status") status: TransferStatus, 
        pageable: Pageable
    ): Page<Transfer>
    
    /**
     * 특정 기간 내 사용자의 송금 내역 조회
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE (t.fromUserId = :userId OR t.toUserId = :userId) 
        AND t.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY t.createdAt DESC
    """)
    fun findTransferHistoryByUserIdAndDateRange(
        @Param("userId") userId: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transfer>
    
    /**
     * 사용자의 일일 송금 총액 조회
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t 
        WHERE t.fromUserId = :userId 
        AND t.status = 'COMPLETED' 
        AND DATE(t.createdAt) = DATE(:date)
    """)
    fun getDailyTransferAmount(@Param("userId") userId: String, @Param("date") date: LocalDateTime): Long
    
    /**
     * 특정 계좌의 일일 송금 총액 조회
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t 
        WHERE t.fromAccountId = :accountId 
        AND t.status = 'COMPLETED' 
        AND DATE(t.createdAt) = DATE(:date)
    """)
    fun getDailyTransferAmountByAccount(@Param("accountId") accountId: String, @Param("date") date: LocalDateTime): Long
    
    /**
     * BLE 송금 내역 조회
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE t.transferType = 'BLE' 
        AND (t.fromUserId = :userId OR t.toUserId = :userId) 
        ORDER BY t.createdAt DESC
    """)
    fun findBleTransfersByUserId(@Param("userId") userId: String, pageable: Pageable): Page<Transfer>
    
    /**
     * 송금코드로 거래 내역 조회
     */
    fun findByTransferCode(transferCode: String): Optional<Transfer>
    
    /**
     * 실패한 송금 내역 조회 (재시도 목적)
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE t.status = 'FAILED' 
        AND t.createdAt >= :since 
        ORDER BY t.createdAt DESC
    """)
    fun findFailedTransfersSince(@Param("since") since: LocalDateTime): List<Transfer>
    
    /**
     * 특정 사용자 간 송금 내역 조회
     */
    @Query("""
        SELECT t FROM Transfer t 
        WHERE ((t.fromUserId = :userId1 AND t.toUserId = :userId2) 
        OR (t.fromUserId = :userId2 AND t.toUserId = :userId1)) 
        ORDER BY t.createdAt DESC
    """)
    fun findTransfersBetweenUsers(
        @Param("userId1") userId1: String, 
        @Param("userId2") userId2: String, 
        pageable: Pageable
    ): Page<Transfer>
    
    /**
     * 송금 통계 - 월별 송금 건수
     */
    @Query("""
        SELECT COUNT(t) FROM Transfer t 
        WHERE t.fromUserId = :userId 
        AND t.status = 'COMPLETED' 
        AND YEAR(t.createdAt) = :year 
        AND MONTH(t.createdAt) = :month
    """)
    fun getMonthlyTransferCount(@Param("userId") userId: String, @Param("year") year: Int, @Param("month") month: Int): Long
    
    /**
     * 송금 통계 - 월별 송금 총액
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t 
        WHERE t.fromUserId = :userId 
        AND t.status = 'COMPLETED' 
        AND YEAR(t.createdAt) = :year 
        AND MONTH(t.createdAt) = :month
    """)
    fun getMonthlyTransferAmount(@Param("userId") userId: String, @Param("year") year: Int, @Param("month") month: Int): Long
}