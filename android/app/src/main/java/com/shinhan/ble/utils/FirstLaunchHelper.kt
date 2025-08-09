package com.shinhan.ble.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 첫 실행 감지 도우미 클래스
 */
object FirstLaunchHelper {
    
    private const val PREFS_NAME = "app_launch_status"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_DEVICE_REGISTERED = "device_registered"
    private const val KEY_LAST_LAUNCH_TIME = "last_launch_time"
    private const val KEY_LAUNCH_COUNT = "launch_count"
    
    private const val TAG = "FirstLaunchHelper"
    
    /**
     * 첫 번째 실행인지 확인
     */
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * 첫 번째 실행 완료 표시
     */
    fun markFirstLaunchCompleted(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit()
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .putLong(KEY_LAST_LAUNCH_TIME, System.currentTimeMillis())
            .putInt(KEY_LAUNCH_COUNT, getLaunchCount(context) + 1)
            .apply()
        
        Log.i(TAG, "First launch completed and marked")
    }
    
    /**
     * 디바이스 등록 완료 여부 확인
     */
    fun isDeviceRegistered(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_DEVICE_REGISTERED, false)
    }
    
    /**
     * 디바이스 등록 완료 표시
     */
    fun markDeviceRegistered(context: Context, deviceId: String? = null) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
            .putBoolean(KEY_DEVICE_REGISTERED, true)
            .putLong(KEY_LAST_LAUNCH_TIME, System.currentTimeMillis())
        
        deviceId?.let {
            editor.putString("registered_device_id", it)
        }
        
        editor.apply()
        Log.i(TAG, "Device registration completed and marked")
    }
    
    /**
     * 등록된 디바이스 ID 가져오기
     */
    fun getRegisteredDeviceId(context: Context): String? {
        val prefs = getPreferences(context)
        return prefs.getString("registered_device_id", null)
    }
    
    /**
     * 앱 실행 횟수 가져오기
     */
    fun getLaunchCount(context: Context): Int {
        val prefs = getPreferences(context)
        return prefs.getInt(KEY_LAUNCH_COUNT, 0)
    }
    
    /**
     * 마지막 실행 시간 가져오기
     */
    fun getLastLaunchTime(context: Context): Long {
        val prefs = getPreferences(context)
        return prefs.getLong(KEY_LAST_LAUNCH_TIME, 0)
    }
    
    /**
     * 실행 횟수 증가
     */
    fun incrementLaunchCount(context: Context) {
        val prefs = getPreferences(context)
        val currentCount = getLaunchCount(context)
        prefs.edit()
            .putInt(KEY_LAUNCH_COUNT, currentCount + 1)
            .putLong(KEY_LAST_LAUNCH_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 디바이스 등록이 필요한지 확인
     */
    fun needsDeviceRegistration(context: Context): Boolean {
        return isFirstLaunch(context) || !isDeviceRegistered(context)
    }
    
    /**
     * 모든 상태 재설정 (테스트용)
     */
    fun resetAllStatus(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit().clear().apply()
        Log.w(TAG, "All launch status has been reset")
    }
    
    /**
     * 현재 상태 정보 가져오기 (디버깅용)
     */
    fun getStatusInfo(context: Context): String {
        val prefs = getPreferences(context)
        return buildString {
            append("First Launch: ${isFirstLaunch(context)}\n")
            append("Device Registered: ${isDeviceRegistered(context)}\n")
            append("Launch Count: ${getLaunchCount(context)}\n")
            append("Last Launch: ${getLastLaunchTime(context)}\n")
            append("Registered Device ID: ${getRegisteredDeviceId(context)}")
        }
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}