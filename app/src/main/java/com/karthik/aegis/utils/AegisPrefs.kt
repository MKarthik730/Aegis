package com.karthik.aegis.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("aegis_prefs")

class AegisPrefs(private val context: Context) {

    companion object {
        private val LOCATION_TRACKING_ENABLED = booleanPreferencesKey("location_tracking_enabled")
        private val ACCIDENT_DETECTION_ENABLED = booleanPreferencesKey("accident_detection_enabled")
        private val FATIGUE_DETECTION_ENABLED = booleanPreferencesKey("fatigue_detection_enabled")
        private val HOME_WIFI_SSID = stringPreferencesKey("home_wifi_ssid")
        private val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        private val EMERGENCY_CONTACTS_COUNT = stringPreferencesKey("emergency_contacts_count")
    }

    fun isLocationTrackingEnabled(): Boolean {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getBoolean(LOCATION_TRACKING_ENABLED.name, false)
    }

    fun setLocationTrackingEnabled(enabled: Boolean) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putBoolean(LOCATION_TRACKING_ENABLED.name, enabled).apply()
    }

    fun isAccidentDetectionEnabled(): Boolean {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getBoolean(ACCIDENT_DETECTION_ENABLED.name, true)
    }

    fun setAccidentDetectionEnabled(enabled: Boolean) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putBoolean(ACCIDENT_DETECTION_ENABLED.name, enabled).apply()
    }

    fun isFatigueDetectionEnabled(): Boolean {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getBoolean(FATIGUE_DETECTION_ENABLED.name, false)
    }

    fun setFatigueDetectionEnabled(enabled: Boolean) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putBoolean(FATIGUE_DETECTION_ENABLED.name, enabled).apply()
    }

    fun getHomeWifiSSID(): String? {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getString(HOME_WIFI_SSID.name, null)
    }

    fun setHomeWifiSSID(ssid: String) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putString(HOME_WIFI_SSID.name, ssid).apply()
    }

    fun isNightModeEnabled(): Boolean {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getBoolean(NIGHT_MODE_ENABLED.name, true)
    }

    fun setNightModeEnabled(enabled: Boolean) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putBoolean(NIGHT_MODE_ENABLED.name, enabled).apply()
    }

    fun getUserFCMToken(): String? {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getString("fcm_token", null)
    }

    fun setUserFCMToken(token: String) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()
    }

    fun getCurrentUserId(): String? {
        return context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .getString("current_user_id", null)
    }

    fun setCurrentUserId(userId: String) {
        context.getSharedPreferences("aegis", Context.MODE_PRIVATE)
            .edit().putString("current_user_id", userId).apply()
    }
}
