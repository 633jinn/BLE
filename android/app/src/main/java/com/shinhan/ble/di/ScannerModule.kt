package com.shinhan.ble.di

import com.shinhan.ble.scanner.BleScanner
import com.shinhan.ble.scanner.MockBleScanner
import com.shinhan.ble.scanner.RealBleScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for BLE scanner configuration
 * Switch between mock and real implementations for testing
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {

    // Use real implementation for production
    @Binds
    @Singleton
    abstract fun bindBleScanner(realBleScanner: RealBleScanner): BleScanner
    
    // Mock implementation for testing - comment out real scanner above and uncomment this for testing
    // @Binds
    // @Singleton
    // abstract fun bindBleScanner(mockBleScanner: MockBleScanner): BleScanner
}

/**
 * Qualifiers for different scanner implementations
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockScanner

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RealScanner

/**
 * Module providing both implementations with qualifiers
 * Use this when you need both implementations available
 */
@Module
@InstallIn(SingletonComponent::class)
object QualifiedScannerModule {
    
    // Provides mock scanner with qualifier
    // @Provides
    // @MockScanner
    // @Singleton
    // fun provideMockBleScanner(mockBleScanner: MockBleScanner): BleScanner = mockBleScanner
    
    // Provides real scanner with qualifier  
    // @Provides
    // @RealScanner
    // @Singleton
    // fun provideRealBleScanner(realBleScanner: RealBleScanner): BleScanner = realBleScanner
}