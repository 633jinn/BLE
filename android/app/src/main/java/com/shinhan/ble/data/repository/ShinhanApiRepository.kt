package com.shinhan.ble.data.repository

import android.util.Log
import com.shinhan.ble.data.network.api.ShinhanApiService
import com.shinhan.ble.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 신한은행 API Repository - 백엔드 서버와 직접 통신
 */
@Singleton
class ShinhanApiRepository @Inject constructor(
    private val apiService: ShinhanApiService
) {
    
    /**
     * 사용자 계좌 목록 조회
     */
    suspend fun getUserAccounts(userId: String): Result<List<AccountDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserAccounts(userId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "계좌 목록 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 대표 계좌 조회
     */
    suspend fun getPrimaryAccount(userId: String): Result<AccountDto?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPrimaryAccount(userId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "대표 계좌 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 계좌 정보 조회
     */
    suspend fun getAccountInfo(accountId: String): Result<AccountDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAccountInfo(accountId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "계좌 정보 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 대표 계좌 설정
     */
    suspend fun setPrimaryAccount(userId: String, accountId: String): Result<AccountDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.setPrimaryAccount(userId, accountId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "대표 계좌 설정 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 계좌 잔액 조회
     */
    suspend fun getAccountBalance(accountId: String, userId: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAccountBalance(accountId, userId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "잔액 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * BLE 송금 처리
     */
    suspend fun processBleTransfer(bleTransferRequest: BleTransferRequestDto, userId: String): Result<TransferProcessResultDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.processBleTransfer(userId, bleTransferRequest)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "BLE 송금 처리 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * BLE 송금코드 생성
     */
    suspend fun generateTransferCode(codeGenerationDto: BleTransferCodeGenerationDto): Result<BleTransferCodeDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateTransferCode(codeGenerationDto)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "송금코드 생성 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * BLE 송금코드 검증
     */
    suspend fun validateTransferCode(transferCode: String): Result<BleTransferCodeValidationResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = BleTransferCodeValidationRequestDto(transferCode)
                val response = apiService.validateTransferCode(request)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "송금코드 검증 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 활성 BLE 송금코드 조회
     */
    suspend fun getActiveTransferCode(userId: String): Result<BleTransferCodeDto?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getActiveTransferCode(userId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "활성 송금코드 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 송금 내역 조회
     */
    suspend fun getTransferHistory(userId: String, page: Int = 0, size: Int = 20): Result<List<TransferHistoryDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTransferHistory(userId, page, size)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data.content)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "송금 내역 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * BLE 송금 내역 조회
     */
    suspend fun getBleTransferHistory(userId: String, page: Int = 0, size: Int = 20): Result<List<TransferHistoryDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBleTransferHistory(userId, page, size)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data.content)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "BLE 송금 내역 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 거래 상세 조회
     */
    suspend fun getTransferDetail(transactionId: String): Result<TransferDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTransferDetail(transactionId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "거래 상세 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 디바이스 등록
     */
    suspend fun registerDevice(deviceRegistrationDto: DeviceRegistrationDto): Result<DeviceInfoDto> {
        Log.d("까끙" ,"까아끙")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.registerDevice(deviceRegistrationDto)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "디바이스 등록 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 디바이스 정보 조회
     */
    suspend fun getDeviceInfo(deviceId: String): Result<DeviceInfoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDeviceInfo(deviceId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "디바이스 정보 조회 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 디바이스 활성 상태 업데이트
     */
    suspend fun pingDevice(deviceId: String): Result<DeviceInfoDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.pingDevice(deviceId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "디바이스 ping 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 새 계좌 생성
     */
    suspend fun createAccount(accountCreateRequest: AccountCreateRequestDto): Result<AccountDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createAccount(accountCreateRequest)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "계좌 생성 실패"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}