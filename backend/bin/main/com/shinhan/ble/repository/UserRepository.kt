package com.shinhan.ble.repository

import com.shinhan.ble.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**import org.springframework.data.jpa.repository.JpaRepository

 * 사용자 정보 Repository
 */
@Repository
interface UserRepository : JpaRepository<User, String> {
    
    /**
     * 전화번호로 사용자 조회
     */
    fun findByPhoneNumber(phoneNumber: String): Optional<User>
    
    /**
     * 고객번호로 사용자 조회
     */
    fun findByCustomerNumber(customerNumber: String): Optional<User>
    
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 활성 사용자 여부 확인
     */
    fun existsByPhoneNumberAndIsActive(phoneNumber: String, isActive: Boolean): Boolean
    
    /**
     * BLE 활성화된 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.bleEnabled = true AND u.isActive = true")
    fun findAllBleEnabledUsers(): List<User>
    
    /**
     * 사용자 ID 목록으로 활성 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.userId IN :userIds AND u.isActive = true")
    fun findActiveUsersByIds(@Param("userIds") userIds: List<String>): List<User>
    
    /**
     * 전화번호 존재 여부 확인
     */
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    
    /**
     * 고객번호 존재 여부 확인
     */
    fun existsByCustomerNumber(customerNumber: String): Boolean
}