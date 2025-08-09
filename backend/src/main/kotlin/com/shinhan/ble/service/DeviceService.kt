package com.shinhan.ble.service

import com.shinhan.ble.dto.*
import com.shinhan.ble.entity.Account
import com.shinhan.ble.entity.Device
import com.shinhan.ble.entity.User
import com.shinhan.ble.repository.AccountRepository
import com.shinhan.ble.repository.DeviceRepository
import com.shinhan.ble.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 디바이스 관리 서비스
 */
@Service
@Transactional
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    /**
     * 디바이스 등록
     */
    fun registerDevice(registrationDto: DeviceRegistrationDto): DeviceInfoDto {
        // 기존 디바이스 ID가 있는지 확인
        val existingDevice = registrationDto.deviceId?.let { deviceId ->
            deviceRepository.findByDeviceId(deviceId).orElse(null)
        }
        
        return if (existingDevice != null) {
            // 기존 디바이스 정보 업데이트
            existingDevice.deviceName = registrationDto.deviceName
            existingDevice.deviceModel = registrationDto.deviceModel
            existingDevice.osVersion = registrationDto.osVersion
            existingDevice.appVersion = registrationDto.appVersion
            existingDevice.userId = registrationDto.userId ?: existingDevice.userId
            existingDevice.updateLastActive()
            
            val savedDevice = deviceRepository.save(existingDevice)
            convertToDto(savedDevice)
        } else {
            // 새 디바이스 등록 - User와 Account도 자동 생성
            val userId = registrationDto.userId
            if (!userId.isNullOrBlank()) {
                createUserAndAccountIfNotExists(userId)
            }
            
            val newDevice = Device.from(registrationDto)
            val savedDevice = deviceRepository.save(newDevice)
            convertToDto(savedDevice)
        }
    }
    
    /**
     * 디바이스 정보 조회
     */
    @Transactional(readOnly = true)
    fun getDeviceInfo(deviceId: String): DeviceInfoDto? {
        return deviceRepository.findByDeviceId(deviceId)
            .map { convertToDto(it) }
            .orElse(null)
    }
    
    /**
     * 사용자의 디바이스 목록 조회
     */
    @Transactional(readOnly = true)
    fun getUserDevices(userId: String): List<DeviceInfoDto> {
        return deviceRepository.findByUserId(userId)
            .map { convertToDto(it) }
    }
    
    /**
     * 디바이스 정보 업데이트
     */
    fun updateDevice(deviceId: String, updateDto: DeviceUpdateDto): DeviceInfoDto? {
        val device = deviceRepository.findByDeviceId(deviceId).orElse(null)
            ?: return null
        
        device.updateInfo(updateDto)
        val savedDevice = deviceRepository.save(device)
        return convertToDto(savedDevice)
    }
    
    /**
     * 디바이스 활성 상태 업데이트
     */
    fun updateDeviceActivity(deviceId: String): DeviceInfoDto? {
        val device = deviceRepository.findByDeviceId(deviceId).orElse(null)
            ?: return null
        
        device.updateLastActive()
        val savedDevice = deviceRepository.save(device)
        return convertToDto(savedDevice)
    }
    
    /**
     * 디바이스 삭제
     */
    fun deleteDevice(deviceId: String): Boolean {
        val device = deviceRepository.findByDeviceId(deviceId).orElse(null)
            ?: return false
        
        deviceRepository.delete(device)
        return true
    }
    
    /**
     * User와 Account가 존재하지 않으면 자동 생성
     */
    private fun createUserAndAccountIfNotExists(userId: String) {
        // User가 존재하지 않으면 생성
        val existingUser = userRepository.findById(userId).orElse(null)
        if (existingUser == null) {
            val newUser = User(
                userId = userId,
                customerNumber = generateCustomerNumber(),
                userName = "사용자${userId.take(6)}", // 임시 사용자명
                phoneNumber = generatePhoneNumber(),
                email = null,
                passwordHash = passwordEncoder.encode("defaultPassword123"), // 기본 비밀번호
                isActive = true,
                bleEnabled = true
            )
            userRepository.save(newUser)
        }
        
        // Account가 존재하지 않으면 기본 계좌 생성
        val existingAccounts = accountRepository.findActiveAccountsByUserId(userId)
        if (existingAccounts.isEmpty()) {
            val accountNumber = generateAccountNumber("088") // 신한은행 코드
            val newAccount = Account(
                accountId = UUID.randomUUID().toString(),
                userId = userId,
                accountNumber = accountNumber,
                accountType = "입출금통장",
                bankName = "신한은행",
                bankCode = "088",
                balance = 1000000L, // 기본 잔액 100만원
                currency = "KRW",
                isActive = true,
                isPrimary = true,
                dailyTransferLimit = 10000000L, // 1천만원
                singleTransferLimit = 2000000L // 200만원
            )
            accountRepository.save(newAccount)
        }
    }
    
    /**
     * 고객번호 생성
     */
    private fun generateCustomerNumber(): String {
        return "CUST${System.currentTimeMillis() % 100000000}"
    }
    
    /**
     * 임시 전화번호 생성
     */
    private fun generatePhoneNumber(): String {
        val randomNumber = Random().nextInt(90000000) + 10000000
        return "010${randomNumber}"
    }
    
    /**
     * 계좌번호 생성
     */
    private fun generateAccountNumber(bankCode: String): String {
        val timestamp = System.currentTimeMillis()
        val randomPart = Random().nextInt(1000)
        return "$bankCode-${timestamp % 100000}-${String.format("%03d", randomPart)}"
    }
    
    /**
     * 디바이스 엔티티를 DTO로 변환
     */
    private fun convertToDto(device: Device): DeviceInfoDto {
        return DeviceInfoDto(
            id = device.id.toString(),
            deviceName = device.deviceName,
            deviceModel = device.deviceModel,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
            deviceId = device.deviceId,
            userId = device.userId,
            registeredAt = device.registeredAt.toString(),
            lastActiveAt = device.lastActiveAt.toString()
        )
    }
}