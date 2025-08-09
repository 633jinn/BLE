package com.shinhan.ble.data.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 네트워크 에러 처리 인터셉터
 */
class ErrorInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            val response = chain.proceed(request)
            
            // HTTP 에러 코드별 로깅
            when (response.code) {
                400 -> Log.w(TAG, "Bad Request: ${request.url}")
                401 -> Log.w(TAG, "Unauthorized: ${request.url}")
                403 -> Log.w(TAG, "Forbidden: ${request.url}")
                404 -> Log.w(TAG, "Not Found: ${request.url}")
                500 -> Log.e(TAG, "Internal Server Error: ${request.url}")
                502 -> Log.e(TAG, "Bad Gateway: ${request.url}")
                503 -> Log.e(TAG, "Service Unavailable: ${request.url}")
            }
            
            return response
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error for ${request.url}: ${e.message}", e)
            throw e
        }
    }
    
    companion object {
        private const val TAG = "ErrorInterceptor"
    }
}