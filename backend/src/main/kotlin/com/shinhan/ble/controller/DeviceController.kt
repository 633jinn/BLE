package com.shinhan.ble.controller

import com.shinhan.ble.dto.*
import com.shinhan.ble.service.DeviceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 디바이스 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = ["*"])
class DeviceController(
    private val deviceService: DeviceService
) {
    
    /**
     * 디바이스 등록/업데이트
     */
    @PostMapping("/register")
    fun registerDevice(@RequestBody registrationDto: DeviceRegistrationDto): ResponseEntity<ShinhanApiResponse<DeviceInfoDto>> {
        return try {
            val deviceInfo = deviceService.registerDevice(registrationDto)
            ResponseEntity.ok(
                ShinhanApiResponse.success(deviceInfo, "디바이스가 성공적으로 등록되었습니다")
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ShinhanApiResponse.error(e.message ?: "잘못된 요청입니다")
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("디바이스 등록 중 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 디바이스 정보 조회
     */
    @GetMapping("/{deviceId}")
    fun getDeviceInfo(@PathVariable deviceId: String): ResponseEntity<ShinhanApiResponse<DeviceInfoDto>> {
        return try {
            val deviceInfo = deviceService.getDeviceInfo(deviceId)
            if (deviceInfo != null) {
                ResponseEntity.ok(ShinhanApiResponse.success(deviceInfo))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("디바이스 정보 조회 중 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 사용자의 디바이스 목록 조회
     */
    @GetMapping("/user/{userId}")
    fun getUserDevices(@PathVariable userId: String): ResponseEntity<ShinhanApiResponse<List<DeviceInfoDto>>> {
        return try {
            val devices = deviceService.getUserDevices(userId)
            ResponseEntity.ok(ShinhanApiResponse.success(devices))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("사용자 디바이스 목록 조회 중 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 디바이스 정보 업데이트
     */
    @PutMapping("/{deviceId}")
    fun updateDevice(
        @PathVariable deviceId: String,
        @RequestBody updateDto: DeviceUpdateDto
    ): ResponseEntity<ShinhanApiResponse<DeviceInfoDto>> {
        return try {
            val updatedDevice = deviceService.updateDevice(deviceId, updateDto)
            if (updatedDevice != null) {
                ResponseEntity.ok(
                    ShinhanApiResponse.success(updatedDevice, "디바이스 정보가 업데이트되었습니다")
                )
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("디바이스 정보 업데이트 중 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 디바이스 활성 상태 업데이트 (ping)
     */
    @PostMapping("/{deviceId}/ping")
    fun pingDevice(@PathVariable deviceId: String): ResponseEntity<ShinhanApiResponse<DeviceInfoDto>> {
        return try {
            val updatedDevice = deviceService.updateDeviceActivity(deviceId)
            if (updatedDevice != null) {
                ResponseEntity.ok(ShinhanApiResponse.success(updatedDevice))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("디바이스 활성 상태 업데이트 중 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 디바이스 삭제
     */
    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable deviceId: String): ResponseEntity<ShinhanApiResponse<Unit>> {
        return try {
            val deleted = deviceService.deleteDevice(deviceId)
            if (deleted) {
                ResponseEntity.ok(ShinhanApiResponse.success("디바이스가 삭제되었습니다"))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ShinhanApiResponse.notFound("디바이스를 찾을 수 없습니다")
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ShinhanApiResponse.error("디바이스 삭제 중 오류가 발생했습니다")
            )
        }
    }
}