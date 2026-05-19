package com.karthik.aegis.utils

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AegisPrefs @Inject constructor(private val context: Context) {

    companion object {
        private const val PREF_NAME = "aegis_settings"
        private const val KEY_LOCATION_TRACKING = "location_tracking_enabled"
        private const val KEY_ACCIDENT_DETECTION = "accident_detection_enabled"
        private const val KEY_FATIGUE_DETECTION = "fatigue_detection_enabled"
        private const val KEY_HOME_WIFI = "home_wifi_ssid"
        private const val KEY_NIGHT_MODE = "night_mode_enabled"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_USER_ID = "current_user_id"
        private const val KEY_FAMILY_GROUP_ID = "family_group_id"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isLocationTrackingEnabled(): Boolean = prefs.getBoolean(KEY_LOCATION_TRACKING, false)
    fun setLocationTrackingEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_LOCATION_TRACKING, enabled).apply()

    fun isAccidentDetectionEnabled(): Boolean = prefs.getBoolean(KEY_ACCIDENT_DETECTION, true)
    fun setAccidentDetectionEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_ACCIDENT_DETECTION, enabled).apply()

    fun isFatigueDetectionEnabled(): Boolean = prefs.getBoolean(KEY_FATIGUE_DETECTION, false)
    fun setFatigueDetectionEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FATIGUE_DETECTION, enabled).apply()

    fun getHomeWifiSSID(): String? = prefs.getString(KEY_HOME_WIFI, null)
    fun setHomeWifiSSID(ssid: String) = prefs.edit().putString(KEY_HOME_WIFI, ssid).apply()

    fun isNightModeEnabled(): Boolean = prefs.getBoolean(KEY_NIGHT_MODE, true)
    fun setNightModeEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()

    fun getUserFCMToken(): String? = prefs.getString(KEY_FCM_TOKEN, null)
    fun setUserFCMToken(token: String) = prefs.edit().putString(KEY_FCM_TOKEN, token).apply()

    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun setCurrentUserId(userId: String) = prefs.edit().putString(KEY_USER_ID, userId).apply()

    fun getFamilyGroupId(): String? = prefs.getString(KEY_FAMILY_GROUP_ID, null)
    fun setFamilyGroupId(groupId: String) = prefs.edit().putString(KEY_FAMILY_GROUP_ID, groupId).apply()
}
