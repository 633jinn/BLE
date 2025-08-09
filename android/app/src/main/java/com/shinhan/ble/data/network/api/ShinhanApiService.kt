package com.shinhan.ble.data.network.api

import com.shinhan.ble.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 신한은행 백엔드 API 서비스 인터페이스 - 백엔드 서버와 매칭
 */
interface ShinhanApiService {
    
    // Account APIs
    /**
     * 사용자 계좌 목록 조회
     */
    @GET("accounts/user/{userId}")
    suspend fun getUserAccounts(@Path("userId") userId: String): Response<ShinhanApiResponse<List<AccountDto>>>

    /**
     * 대표 계좌 조회
     */
    @GET("accounts/user/{userId}/primary")
    suspend fun getPrimaryAccount(@Path("userId") userId: String): Response<ShinhanApiResponse<AccountDto?>>
    
    /**
     * 계좌 정보 조회
     */
    @GET("accounts/{accountId}")
    suspend fun getAccountInfo(@Path("accountId") accountId: String): Response<ShinhanApiResponse<AccountDto>>
    
    /**
     * 계좌 잔액 조회
     */
    @GET("accounts/{accountId}/balance")
    suspend fun getAccountBalance(
        @Path("accountId") accountId: String,
        @Query("userId") userId: String
    ): Response<ShinhanApiResponse<Long>>

    /**
     * 대표 계좌 설정
     */
    @PUT("accounts/user/{userId}/primary/{accountId}")
    suspend fun setPrimaryAccount(
        @Path("userId") userId: String,
        @Path("accountId") accountId: String
    ): Response<ShinhanApiResponse<AccountDto>>
    
    // Transfer APIs
    /**
     * BLE 송금 처리
     */
    @POST("transfers/ble/{userId}")
    suspend fun processBleTransfer(
        @Path("userId") userId: String,
        @Body request: BleTransferRequestDto
    ): Response<ShinhanApiResponse<TransferProcessResultDto>>
    
    /**
     * 송금 내역 조회
     */
    @GET("transfers/history/{userId}")
    suspend fun getTransferHistory(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ShinhanApiResponse<PagedResponse<TransferHistoryDto>>>
    
    /**
     * BLE 송금 내역 조회
     */
    @GET("transfers/history/{userId}/ble")
    suspend fun getBleTransferHistory(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ShinhanApiResponse<PagedResponse<TransferHistoryDto>>>
    
    /**
     * 거래 상세 조회
     */
    @GET("transfers/{transactionId}")
    suspend fun getTransferDetail(@Path("transactionId") transactionId: String): Response<ShinhanApiResponse<TransferDetailDto>>
    
    // BLE Transfer Code APIs
    /**
     * BLE 송금코드 생성
     */
    @POST("transfers/ble-codes")
    suspend fun generateTransferCode(@Body request: BleTransferCodeGenerationDto): Response<ShinhanApiResponse<BleTransferCodeDto>>
    
    /**
     * BLE 송금코드 검증
     */
    @POST("transfers/ble-codes/validate")
    suspend fun validateTransferCode(@Body request: BleTransferCodeValidationRequestDto): Response<ShinhanApiResponse<BleTransferCodeValidationResult>>
    
    /**
     * 활성 BLE 송금코드 조회
     */
    @GET("transfers/ble-codes/active/{userId}")
    suspend fun getActiveTransferCode(@Path("userId") userId: String): Response<ShinhanApiResponse<BleTransferCodeDto?>>
    
    /**
     * 사용자의 모든 BLE 송금코드 조회
     */
    @GET("transfers/ble-codes/user/{userId}")
    suspend fun getUserTransferCodes(@Path("userId") userId: String): Response<ShinhanApiResponse<List<BleTransferCodeDto>>>
    
    /**
     * BLE 송금코드 비활성화
     */
    @DELETE("transfers/ble-codes/{codeId}")
    suspend fun deactivateTransferCode(
        @Path("codeId") codeId: String,
        @Query("userId") userId: String
    ): Response<ShinhanApiResponse<String>>
    
    // Device APIs
    /**
     * 디바이스 등록/업데이트
     */
    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationDto): Response<ShinhanApiResponse<DeviceInfoDto>>
    
    /**
     * 디바이스 정보 조회
     */
    @GET("devices/{deviceId}")
    suspend fun getDeviceInfo(@Path("deviceId") deviceId: String): Response<ShinhanApiResponse<DeviceInfoDto>>
    
    /**
     * 사용자의 디바이스 목록 조회
     */
    @GET("devices/user/{userId}")
    suspend fun getUserDevices(@Path("userId") userId: String): Response<ShinhanApiResponse<List<DeviceInfoDto>>>
    
    /**
     * 디바이스 활성 상태 업데이트 (ping)
     */
    @POST("devices/{deviceId}/ping")
    suspend fun pingDevice(@Path("deviceId") deviceId: String): Response<ShinhanApiResponse<DeviceInfoDto>>
    
    /**
     * 새 계좌 생성
     */
    @POST("accounts")
    suspend fun createAccount(@Body request: AccountCreateRequestDto): Response<ShinhanApiResponse<AccountDto>>
}

/**
 * API 엔드포인트 상수
 */
object ShinhanApiEndpoints {
    // 로컬 개발용 백엔드 서버 (context-path: /api/v1 포함)
    const val LOCAL_BASE_URL = "http://10.0.2.2:8080/api/v1/" //애뮬레이터용
    
    // 개발 서버
    const val DEV_BASE_URL = "http://192.168.219.107:8080/api/v1/" //TODO: 본인 와이파이의 ip로 변경 (network_security_config에도 반영)
    
    // 운영 서버
    const val PROD_BASE_URL = "https://api.shinhan.com/"
}