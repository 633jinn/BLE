package com.shinhan.ble.repository

import com.shinhan.ble.entity.BleTransferCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * BLE 송금코드 Repository
 */
@Repository
interface BleTransferCodeRepository : JpaRepository<BleTransferCode, String> {
    
    /**
     * 송금코드로 조회
     */
    fun findByTransferCode(transferCode: String): Optional<BleTransferCode>
    
    /**
     * 사용자 ID로 활성 송금코드 조회
     */
    @Query("""
        SELECT btc FROM BleTransferCode btc 
        WHERE btc.userId = :userId 
        AND btc.isActive = true 
        AND btc.expiresAt > :now 
        AND btc.usedAt IS NULL 
        ORDER BY btc.createdAt DESC
    """)
    fun findActiveCodeByUserId(@Param("userId") userId: String, @Param("now") now: LocalDateTime): Optional<BleTransferCode>
    
    /**
     * 사용자의 모든 송금코드 조회 (최신순)
     */
    @Query("""
        SELECT btc FROM BleTransferCode btc 
        WHERE btc.userId = :userId 
        ORDER BY btc.createdAt DESC
    """)
    fun findAllByUserId(@Param("userId") userId: String): List<BleTransferCode>
    
    /**
     * 유효한 송금코드 조회 (만료되지 않고 사용되지 않은 것)
     */
    @Query("""
        SELECT btc FROM BleTransferCode btc 
        WHERE btc.transferCode = :transferCode 
        AND btc.isActive = true 
        AND btc.expiresAt > :now 
        AND btc.usedAt IS NULL
    """)
    fun findValidTransferCode(@Param("transferCode") transferCode: String, @Param("now") now: LocalDateTime): Optional<BleTransferCode>
    
    /**
     * 송금코드 사용 처리
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE BleTransferCode btc 
        SET btc.usedAt = :usedAt 
        WHERE btc.transferCode = :transferCode
    """)
    fun markAsUsed(@Param("transferCode") transferCode: String, @Param("usedAt") usedAt: LocalDateTime): Int
    
    /**
     * 만료된 송금코드 비활성화
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE BleTransferCode btc 
        SET btc.isActive = false 
        WHERE btc.expiresAt <= :now 
        AND btc.isActive = true
    """)
    fun deactivateExpiredCodes(@Param("now") now: LocalDateTime): Int
    
    /**
     * 사용자의 기존 활성 코드 비활성화 (새 코드 생성 전)
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE BleTransferCode btc 
        SET btc.isActive = false 
        WHERE btc.userId = :userId 
        AND btc.isActive = true 
        AND btc.usedAt IS NULL
    """)
    fun deactivateUserActiveCodes(@Param("userId") userId: String): Int
    
    /**
     * 만료된 송금코드 삭제 (데이터 정리용)
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM BleTransferCode btc 
        WHERE btc.expiresAt <= :cutoffDate
    """)
    fun deleteExpiredCodes(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    /**
     * 송금코드 존재 여부 확인
     */
    fun existsByTransferCode(transferCode: String): Boolean
    
    /**
     * 사용자의 활성 송금코드 개수 조회
     */
    @Query("""
        SELECT COUNT(btc) FROM BleTransferCode btc 
        WHERE btc.userId = :userId 
        AND btc.isActive = true 
        AND btc.expiresAt > :now 
        AND btc.usedAt IS NULL
    """)
    fun countActiveCodesByUserId(@Param("userId") userId: String, @Param("now") now: LocalDateTime): Long
    
    /**
     * 특정 기간 내 생성된 송금코드 조회
     */
    @Query("""
        SELECT btc FROM BleTransferCode btc 
        WHERE btc.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY btc.createdAt DESC
    """)
    fun findCodesByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<BleTransferCode>
}