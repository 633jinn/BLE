package com.shinhan.ble.di

import android.content.Context
import com.google.gson.Gson
import android.os.Build
import com.shinhan.ble.BuildConfig
import com.google.gson.GsonBuilder
import com.shinhan.ble.data.network.api.ShinhanApiEndpoints
import com.shinhan.ble.data.network.api.ShinhanApiService
import com.shinhan.ble.data.network.interceptor.AuthInterceptor
import com.shinhan.ble.data.network.interceptor.ErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Gson 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setLenient()
            .create()
    }
    
    /**
     * HTTP 로깅 인터셉터 제공
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // 개발 중이므로 BODY 레벨로 설정
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * 인증 인터셉터 제공
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(context)
    }
    
    /**
     * 에러 처리 인터셉터 제공
     */
    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor {
        return ErrorInterceptor()
    }
    
    /**
     * OkHttpClient 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)       // 인증 토큰 자동 추가
            .addInterceptor(errorInterceptor)      // 에러 처리
            .addInterceptor(loggingInterceptor)    // 로깅 (마지막에 추가)
            .build()
    }
    
    /**
     * Retrofit 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        val baseUrl = BuildConfig.DEV_BASE_URL
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun isEmulator(): Boolean {
        val fp = Build.FINGERPRINT
        val model = Build.MODEL
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return fp.startsWith("generic") || fp.lowercase().contains("vbox") ||
                fp.lowercase().contains("test-keys") || model.contains("Emulator") ||
                model.contains("Android SDK built for x86") || (brand.startsWith("generic") && device.startsWith("generic")) ||
                product == "google_sdk"
    }
    
    /**
     * 신한은행 API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideShinhanApiService(retrofit: Retrofit): ShinhanApiService {
        return retrofit.create(ShinhanApiService::class.java)
    }
}