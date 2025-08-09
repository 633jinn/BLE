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
     * 계좌 정보 조회
     */
    suspend fun getAccountInfo(accountId: String) = apiRepository.getAccountInfo(accountId)
    
    /**
     * 계좌 잔액 조회
     */
    suspend fun getAccountBalance(accountId: String, userId: String) = apiRepository.getAccountBalance(accountId, userId)
    
    /**
     * BLE 송금 처리
     */
    suspend fun processBleTransfer(bleTransferRequest: BleTransferRequestDto, userId: String) = apiRepository.processBleTransfer(bleTransferRequest, userId)
    
    /**
     * BLE 송금코드 생성
     */
    suspend fun generateTransferCode(codeGenerationDto: BleTransferCodeGenerationDto) = apiRepository.generateTransferCode(codeGenerationDto)
    
    /**
     * 대표 계좌 설정
     */
    suspend fun setPrimaryAccount(userId: String, accountId: String) = apiRepository.setPrimaryAccount(userId, accountId)
    
    /**
     * BLE 송금코드 검증
     */
    suspend fun validateTransferCode(transferCode: String) = apiRepository.validateTransferCode(transferCode)
    
    /**
     * 활성 BLE 송금코드 조회
     */
    suspend fun getActiveTransferCode(userId: String) = apiRepository.getActiveTransferCode(userId)
    
    /**
     * 송금 내역 조회
     */
    suspend fun getTransferHistory(userId: String, page: Int = 0, size: Int = 20) = apiRepository.getTransferHistory(userId, page, size)
    
    /**
     * BLE 송금 내역 조회
     */
    suspend fun getBleTransferHistory(userId: String, page: Int = 0, size: Int = 20) = apiRepository.getBleTransferHistory(userId, page, size)
    
    /**
     * 거래 상세 조회
     */
    suspend fun getTransferDetail(transactionId: String) = apiRepository.getTransferDetail(transactionId)
    
    /**
     * 새 계좌 생성
     */
    suspend fun createAccount(accountCreateRequest: AccountCreateRequestDto) = apiRepository.createAccount(accountCreateRequest)
}