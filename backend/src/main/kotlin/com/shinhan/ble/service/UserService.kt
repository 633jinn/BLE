package com.shinhan.ble.service

import com.shinhan.ble.config.Config
import com.shinhan.ble.dto.UserInfoDto
import com.shinhan.ble.dto.UserRegistrationDto
import com.shinhan.ble.dto.UserSettingsUpdateDto
import com.shinhan.ble.entity.User
import com.shinhan.ble.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


/**
 * 사용자 관리 서비스
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val config: Config,
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * 사용자 등록
     */
    fun registerUser(registrationDto: UserRegistrationDto): UserInfoDto {
        // 중복 체크
        if (userRepository.existsByPhoneNumber(registrationDto.phoneNumber)) {
            throw IllegalArgumentException("이미 등록된 전화번호입니다")
        }
        
        if (userRepository.existsByCustomerNumber(registrationDto.customerNumber)) {
            throw IllegalArgumentException("이미 등록된 고객번호입니다")
        }
        
        // 비밀번호 암호화
        val hashedPassword = passwordEncoder.encode(registrationDto.password)
        
        // 사용자 생성
        val user = User(
            userId = UUID.randomUUID().toString(),
            customerNumber = registrationDto.customerNumber,
            userName = registrationDto.userName,
            phoneNumber = registrationDto.phoneNumber,
            email = registrationDto.email,
            passwordHash = hashedPassword
        )
        
        val savedUser = userRepository.save(user)
        return UserInfoDto.from(savedUser)
    }
    
    /**
     * 전화번호로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun findByPhoneNumber(phoneNumber: String): User? {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null)
    }
    
    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    fun findById(userId: String): User? {
        return userRepository.findById(userId).orElse(null)
    }
    
    /**
     * 고객번호로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun findByCustomerNumber(customerNumber: String): User? {
        return userRepository.findByCustomerNumber(customerNumber).orElse(null)
    }
    
    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUserInfo(userId: String): UserInfoDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        return UserInfoDto.from(user)
    }
    
    /**
     * 비밀번호 검증
     */
    @Transactional(readOnly = true)
    fun validatePassword(user: User, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, user.passwordHash)
    }
    
    /**
     * 사용자 설정 업데이트
     */
    fun updateUserSettings(userId: String, settingsDto: UserSettingsUpdateDto): UserInfoDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        
        val updatedUser = user.copy(
            bleEnabled = settingsDto.bleEnabled ?: user.bleEnabled,
            email = settingsDto.email ?: user.email
        )
        
        val savedUser = userRepository.save(updatedUser)
        return UserInfoDto.from(savedUser)
    }
    
    /**
     * 사용자 비활성화
     */
    fun deactivateUser(userId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        
        val deactivatedUser = user.copy(isActive = false)
        userRepository.save(deactivatedUser)
    }
    
    /**
     * BLE 활성화된 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    fun getBleEnabledUsers(): List<UserInfoDto> {
        return userRepository.findAllBleEnabledUsers()
            .map { UserInfoDto.from(it) }
    }
    
    /**
     * 활성 사용자 여부 확인
     */
    @Transactional(readOnly = true)
    fun isActiveUser(phoneNumber: String): Boolean {
        return userRepository.existsByPhoneNumberAndIsActive(phoneNumber, true)
    }
    
    /**
     * 비밀번호 변경
     */
    fun changePassword(userId: String, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다") }
        
        // 현재 비밀번호 검증
        if (!validatePassword(user, currentPassword)) {
            throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다")
        }
        
        // 새 비밀번호 암호화 및 저장
        val hashedNewPassword = passwordEncoder.encode(newPassword)
        val updatedUser = user.copy(passwordHash = hashedNewPassword)
        userRepository.save(updatedUser)
    }
    
    /**
     * 사용자 ID 목록으로 활성 사용자들 조회
     */
    @Transactional(readOnly = true)
    fun findActiveUsersByIds(userIds: List<String>): List<User> {
        return userRepository.findActiveUsersByIds(userIds)
    }
}