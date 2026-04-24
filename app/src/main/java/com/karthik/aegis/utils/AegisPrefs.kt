package com.karthik.aegis.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aegis_prefs")

@Singleton
class AegisPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.dataStore

    companion object {
        // Feature toggles
        private val KEY_LOCATION_TRACKING    = booleanPreferencesKey("location_tracking")
        private val KEY_ACCIDENT_DETECTION   = booleanPreferencesKey("accident_detection")
        private val KEY_FATIGUE_DETECTION    = booleanPreferencesKey("fatigue_detection")
        private val KEY_ZONE_NOTIFICATIONS   = booleanPreferencesKey("zone_notifications")
        private val KEY_ZONE_EXIT_ALERTS     = booleanPreferencesKey("zone_exit_alerts")
        private val KEY_NIGHT_MODE_SENSITIVITY = booleanPreferencesKey("night_mode_sensitivity")

        // User settings
        private val KEY_HOME_WIFI_SSID       = stringPreferencesKey("home_wifi_ssid")
        private val KEY_USER_NAME            = stringPreferencesKey("user_name")
        private val KEY_USER_PHONE           = stringPreferencesKey("user_phone")
        private val KEY_FAMILY_GROUP_ID      = stringPreferencesKey("family_group_id")
        private val KEY_FCM_TOKEN            = stringPreferencesKey("fcm_token")

        // SOS settings
        private val KEY_AUTO_SOS_COUNTDOWN    = intPreferencesKey("auto_sos_countdown")
        private val KEY_VOLUME_SOS_ENABLED   = booleanPreferencesKey("volume_sos_enabled")
        private val KEY_POWER_SOS_ENABLED    = booleanPreferencesKey("power_sos_enabled")
        private val KEY_SHAKE_SOS_ENABLED    = booleanPreferencesKey("shake_sos_enabled")

        // Location settings
        private val KEY_TRACKING_MODE        = stringPreferencesKey("tracking_mode")
        private val KEY_LOCATION_INTERVAL_MS = longPreferencesKey("location_interval_ms")
        private val KEY_SPEED_ALERT_THRESHOLD = floatPreferencesKey("speed_alert_threshold")

        // Check-in settings
        private val KEY_CHECK_IN_REMINDER_ENABLED = booleanPreferencesKey("check_in_reminder")
        private val KEY_CHECK_IN_INTERVAL_MS      = longPreferencesKey("check_in_interval_ms")

        // Onboarding
        private val KEY_ONBOARDING_COMPLETE  = booleanPreferencesKey("onboarding_complete")
    }

    // ── Feature Toggles ───────────────────────────────────────────────────────

    fun isLocationTrackingEnabled()  = ds.data.map { it[KEY_LOCATION_TRACKING] ?: true }
    fun isAccidentDetectionEnabled() = ds.data.map { it[KEY_ACCIDENT_DETECTION] ?: true }
    fun isFatigueDetectionEnabled()  = ds.data.map { it[KEY_FATIGUE_DETECTION] ?: true }
    fun isZoneNotificationsEnabled() = ds.data.map { it[KEY_ZONE_NOTIFICATIONS] ?: true }
    fun isZoneExitNotificationsEnabled() = ds.data.map { it[KEY_ZONE_EXIT_ALERTS] ?: true }
    fun isNightModeSensitivityEnabled()  = ds.data.map { it[KEY_NIGHT_MODE_SENSITIVITY] ?: false }

    fun getHomeWifiSSID(): String? = runBlocking {
        ds.data.first()[KEY_HOME_WIFI_SSID]
    }

    fun getUserName(): String = runBlocking {
        ds.data.first()[KEY_USER_NAME] ?: ""
    }

    fun getUserPhone(): String = runBlocking {
        ds.data.first()[KEY_USER_PHONE] ?: ""
    }

    fun getFamilyGroupId(): String? = runBlocking {
        ds.data.first()[KEY_FAMILY_GROUP_ID]
    }

    fun getTrackingMode(): String = runBlocking {
        ds.data.first()[KEY_TRACKING_MODE] ?: "PASSIVE"
    }

    fun isOnboardingComplete(): Boolean = runBlocking {
        ds.data.first()[KEY_ONBOARDING_COMPLETE] ?: false
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    suspend fun setLocationTrackingEnabled(enabled: Boolean) {
        ds.edit { it[KEY_LOCATION_TRACKING] = enabled }
    }

    suspend fun setAccidentDetectionEnabled(enabled: Boolean) {
        ds.edit { it[KEY_ACCIDENT_DETECTION] = enabled }
    }

    suspend fun setFatigueDetectionEnabled(enabled: Boolean) {
        ds.edit { it[KEY_FATIGUE_DETECTION] = enabled }
    }

    suspend fun setZoneNotificationsEnabled(enabled: Boolean) {
        ds.edit { it[KEY_ZONE_NOTIFICATIONS] = enabled }
    }

    suspend fun setHomeWifiSSID(ssid: String?) {
        ds.edit {
            if (ssid != null) it[KEY_HOME_WIFI_SSID] = ssid
            else it.remove(KEY_HOME_WIFI_SSID)
        }
    }

    suspend fun setUserName(name: String) {
        ds.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun setUserPhone(phone: String) {
        ds.edit { it[KEY_USER_PHONE] = phone }
    }

    suspend fun setFamilyGroupId(groupId: String?) {
        ds.edit {
            if (groupId != null) it[KEY_FAMILY_GROUP_ID] = groupId
            else it.remove(KEY_FAMILY_GROUP_ID)
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        ds.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setVolumeSOSEnabled(enabled: Boolean) {
        ds.edit { it[KEY_VOLUME_SOS_ENABLED] = enabled }
    }

    suspend fun setPowerSOSEnabled(enabled: Boolean) {
        ds.edit { it[KEY_POWER_SOS_ENABLED] = enabled }
    }

    suspend fun setShakeSOSEnabled(enabled: Boolean) {
        ds.edit { it[KEY_SHAKE_SOS_ENABLED] = enabled }
    }

    suspend fun clearAll() {
        ds.edit { it.clear() }
    }
}