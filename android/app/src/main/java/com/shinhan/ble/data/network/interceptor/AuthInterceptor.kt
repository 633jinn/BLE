package com.shinhan.ble.data.network.interceptor

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import androidx.core.content.edit

/**
 * 신한은행 API 인증 인터셉터
 * Basic Auth와 JWT 토큰을 헤더에 추가하고, 토큰 갱신을 처리
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    companion object {
        // application.yml의 spring.security.user 설정과 동일
        private const val BASIC_AUTH_USERNAME = "admin"
        private const val BASIC_AUTH_PASSWORD = "admin123"
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "shinhan_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 로그인 API는 토큰 없이 요청
        if (originalRequest.url.pathSegments.contains("login") ||
            originalRequest.url.pathSegments.contains("refresh")) {
            return chain.proceed(originalRequest)
        }
        
        // 저장된 액세스 토큰 가져오기
        val accessToken = getAccessToken()
        
        // Basic Authentication 추가
        val basicAuth = Credentials.basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD)
        
        val requestBuilder = originalRequest.newBuilder()
            .header("Authorization", basicAuth)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("X-Client-Version", getAppVersion())
            .header("X-Device-Type", "Android")
        
        // JWT 토큰이 있으면 추가 (Bearer와 Basic 둘 다 보내기)
        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.header("X-JWT-Token", accessToken)
        }
        
        val authenticatedRequest = requestBuilder.build()
        
        val response = chain.proceed(authenticatedRequest)
        
        // 401 Unauthorized인 경우 토큰 갱신 시도
        if (response.code == 401) {
            response.close()
            
            val refreshToken = getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                // 토큰 갱신 시도
                val newAccessToken = refreshAccessToken(refreshToken)
                if (newAccessToken != null) {
                    // 새 토큰으로 재요청
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", basicAuth)
                        .header("X-JWT-Token", newAccessToken)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("X-Client-Version", getAppVersion())
                        .header("X-Device-Type", "Android")
                        .build()
                    
                    return chain.proceed(newRequest)
                } else {
                    // 토큰 갱신 실패 - 로그아웃 처리
                    clearTokens()
                }
            }
        }
        
        return response
    }
    
    /**
     * 저장된 액세스 토큰 가져오기
     */
    private fun getAccessToken(): String? {
        return encryptedPrefs.getString("access_token", null)
    }
    
    /**
     * 저장된 리프레시 토큰 가져오기
     */
    private fun getRefreshToken(): String? {
        return encryptedPrefs.getString("refresh_token", null)
    }
    
    /**
     * 토큰 저장
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("token_saved_at", System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 토큰 삭제 (로그아웃)
     */
    private fun clearTokens() {
        encryptedPrefs.edit {
            remove("access_token")
                .remove("refresh_token")
                .remove("token_saved_at")
        }
    }
    
    /**
     * 액세스 토큰 갱신
     */
    private fun refreshAccessToken(refreshToken: String): String? {
        // TODO: 실제 토큰 갱신 API 호출
        // 현재는 간단히 null 반환 (실제 구현 시 별도 Retrofit 인스턴스 사용)
        return null
    }
    
    /**
     * 앱 버전 가져오기
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}