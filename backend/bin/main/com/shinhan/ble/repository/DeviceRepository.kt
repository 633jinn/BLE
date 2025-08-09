package com.shinhan.ble.repository

import com.shinhan.ble.entity.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * 디바이스 정보 Repository
 */
@Repository
interface DeviceRepository : JpaRepository<Device, Long> {
    
    /**
     * 디바이스 ID로 디바이스 찾기
     */
    fun findByDeviceId(deviceId: String): Optional<Device>
    
    /**
     * 사용자 ID로 디바이스 목록 찾기
     */
    fun findByUserId(userId: String): List<Device>
    
    /**
     * 디바이스 이름으로 디바이스 목록 찾기
     */
    fun findByDeviceNameContaining(deviceName: String): List<Device>
    
    /**
     * 디바이스 ID 존재 여부 확인
     */
    fun existsByDeviceId(deviceId: String): Boolean
    
    /**
     * 사용자 ID와 디바이스 이름으로 디바이스 찾기
     */
    fun findByUserIdAndDeviceName(userId: String, deviceName: String): Optional<Device>
}