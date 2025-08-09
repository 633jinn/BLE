package com.shinhan.ble

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * 신한은행 BLE 송금 서비스 백엔드 애플리케이션
 * 
 * 주요 기능:
 * - 사용자 인증 및 계좌 관리
 * - BLE 송금코드 생성 및 검증
 * - 신한은행 코어뱅킹과 연동한 실제 송금 처리
 * - 송금 내역 관리
 */
@SpringBootApplication
@EntityScan("com.shinhan.ble.entity")
@EnableJpaRepositories("com.shinhan.ble.repository")
class BleBackendApplication

fun main(args: Array<String>) {
    runApplication<BleBackendApplication>(*args)
}