package com.shinhan.ble.data.database

import com.shinhan.ble.data.repository.ShinhanApiRepository
import com.shinhan.ble.data.network.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleTransferRepository @Inject constructor(
    private val apiRepository: ShinhanApiRepository
) {
    
    /**
     * 사용자 계좌 목록 조회
     */
    suspend fun getUserAccounts(userId: String) = apiRepository.getUserAccounts(userId)
    
    /**
     * 대표 계좌 조회
     */
    suspend fun getPrimaryAccount(userId: String) = apiRepository.getPrimaryAccount(userId)

    /**
     * 대표 계좌 설정
     */
    suspend fun setPrimaryAccount(userId: String, accountId: String) = apiRepository.setPrimaryAccount(userId, accountId)

    /**
     * BLE 송금코드 생성
     */
    suspend fun generateTransferCode(codeGenerationDto: BleTransferCodeGenerationDto) = apiRepository.generateTransferCode(codeGenerationDto)

    /**
     * 새 계좌 생성
     */
    suspend fun createAccount(accountCreateRequest: AccountCreateRequestDto) = apiRepository.createAccount(accountCreateRequest)
}