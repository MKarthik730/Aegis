package com.karthik.aegis.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SafeZone(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Double = 150.0,
    val type: String = "SAFE", // SAFE, DANGER, SCHOOL, WORK, HOME
    val isHome: Boolean = false,
    val notifyOnEnter: Boolean = true,
    val notifyOnExit: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class TrackedLocation(
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "PASSIVE",
    val isOnline: Boolean = true
)

@IgnoreExtraProperties
data class FamilyMember(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: String = "MEMBER", // ADMIN, MEMBER
    val status: String = "SAFE", // SAFE, UNSAFE, OFFLINE
    val fcmToken: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = 0L
)

@IgnoreExtraProperties
data class SOSAlert(
    val uid: String = "",
    val senderName: String = "",
    val senderUid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val reason: String = "",
    val isAutomatic: Boolean = false,
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, RESPONDED
    val timestamp: Long = System.currentTimeMillis(),
    val responders: Map<String, Any> = emptyMap()
)

@IgnoreExtraProperties
data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val relation: String = "", // Parent, Sibling, Spouse, Friend
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class AlertHistory(
    val id: String = "",
    val type: String = "", // SOS, ZONE_EXIT, LOW_BATTERY, OFFLINE, ANOMALY
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val memberUid: String = "",
    val isAutomatic: Boolean = false
)

@IgnoreExtraProperties
data class SafetyScore(
    val uid: String = "",
    val weekStart: Long = 0L,
    val weekEnd: Long = 0L,
    val score: Int = 0, // 0-100
    val checkInCount: Int = 0,
    val zoneCompliance: Int = 0, // percentage
    val sosTriggers: Int = 0,
    val safeBehavior: Int = 0,
    val breakdown: Map<String, Any> = emptyMap()
)

@IgnoreExtraProperties
data class CheckIn(
    val uid: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "SAFE",
    val notes: String = ""
)