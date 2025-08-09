package com.shinhan.ble.dto

/**
 * 디바이스 등록 요청 DTO
 */
data class DeviceRegistrationDto(
    val deviceName: String,
    val deviceModel: String?,
    val osVersion: String?,
    val appVersion: String?,
    val deviceId: String? = null,
    val userId: String? = null
)

/**
 * 디바이스 정보 응답 DTO
 */
data class DeviceInfoDto(
    val id: String,
    val deviceName: String,
    val deviceModel: String?,
    val osVersion: String?,
    val appVersion: String?,
    val deviceId: String?,
    val userId: String?,
    val registeredAt: String,
    val lastActiveAt: String
)

/**
 * 디바이스 업데이트 요청 DTO
 */
data class DeviceUpdateDto(
    val deviceName: String?,
    val appVersion: String?
)