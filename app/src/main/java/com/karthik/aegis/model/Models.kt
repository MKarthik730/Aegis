package com.karthik.aegis.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SafeZone(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Double = 150.0,
    val type: String = "SAFE",
    val isHome: Boolean = false,
    val notifyOnEnter: Boolean = true,
    val notifyOnExit: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "tracked_locations")
data class TrackedLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "PASSIVE",
    @ColumnInfo(name = "is_online") val isOnline: Boolean = true
)

@IgnoreExtraProperties
data class FamilyMember(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: String = "MEMBER",
    val status: String = "SAFE",
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
    val status: String = "ACTIVE",
    val timestamp: Long = System.currentTimeMillis(),
    val responders: Map<String, Any> = emptyMap()
)

@IgnoreExtraProperties
data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val relation: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Entity(tableName = "alert_history")
data class AlertHistory(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val id: String = "",
    val type: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "member_uid") val memberUid: String = "",
    @ColumnInfo(name = "is_automatic") val isAutomatic: Boolean = false
)

@IgnoreExtraProperties
@Entity(tableName = "safety_scores")
data class SafetyScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = "",
    @ColumnInfo(name = "week_start") val weekStart: Long = 0L,
    @ColumnInfo(name = "week_end") val weekEnd: Long = 0L,
    val score: Int = 0,
    @ColumnInfo(name = "check_in_count") val checkInCount: Int = 0,
    @ColumnInfo(name = "zone_compliance") val zoneCompliance: Int = 0,
    @ColumnInfo(name = "sos_triggers") val sosTriggers: Int = 0,
    @ColumnInfo(name = "safe_behavior") val safeBehavior: Int = 0
) {
    @Ignore
    var breakdown: Map<String, Any> = emptyMap()
}

@IgnoreExtraProperties
@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val uid: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "SAFE",
    val notes: String = ""
)
