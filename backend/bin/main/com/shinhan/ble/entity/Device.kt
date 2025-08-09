package com.shinhan.ble.entity

import java.time.LocalDateTime
import jakarta.persistence.*
import com.shinhan.ble.dto.DeviceRegistrationDto

/**
 * 디바이스 정보 엔티티
 */
@Entity
@Table(name = "devices")
data class Device(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    var deviceName: String,
    
    @Column
    var deviceModel: String? = null,
    
    @Column
    var osVersion: String? = null,
    
    @Column
    var appVersion: String? = null,
    
    @Column(unique = true)
    var deviceId: String? = null,
    
    @Column
    var userId: String? = null,
    
    @Column(nullable = false)
    val registeredAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var lastActiveAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun from(registrationDto: DeviceRegistrationDto): Device {
            return Device(
                deviceName = registrationDto.deviceName,
                deviceModel = registrationDto.deviceModel,
                osVersion = registrationDto.osVersion,
                appVersion = registrationDto.appVersion,
                deviceId = registrationDto.deviceId,
                userId = registrationDto.userId
            )
        }
    }
    
    fun updateLastActive() {
        lastActiveAt = LocalDateTime.now()
    }
    
    fun updateInfo(updateDto: com.shinhan.ble.dto.DeviceUpdateDto) {
        updateDto.deviceName?.let { deviceName = it }
        updateDto.appVersion?.let { appVersion = it }
        updateLastActive()
    }
}