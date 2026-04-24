package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.aegis.model.EmergencyContact
import com.karthik.aegis.model.SOSAlert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SOSRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun triggerSOSWithReason(
        contacts: List<EmergencyContact>,
        reason: String,
        isAutomatic: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onFailure("User not logged in")
            return
        }

        try {
            val alert = mapOf(
                "uid"          to uid,
                "senderName"   to auth.currentUser?.displayName ?: "Family Member",
                "senderUid"    to uid,
                "latitude"     to 0.0, // Will be filled by service
                "longitude"    to 0.0,
                "reason"       to reason,
                "isAutomatic"  to isAutomatic,
                "status"       to "ACTIVE",
                "timestamp"    to System.currentTimeMillis()
            )

            realtimeDb.child("sos_alerts").child(uid).setValue(alert).await()

            // Queue FCM notifications
            val fcmData = mapOf(
                "title"     to "🆘 SOS Alert!",
                "body"      to "$reason — ${auth.currentUser?.displayName ?: "Family Member"} needs help!",
                "tokens"    to contacts.map { it.fcmToken },
                "alertUid"  to uid
            )
            firestore.collection("fcm_queue").add(fcmData).await()

            onSuccess()
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown error")
        }
    }

    suspend fun resolveSOSAlert(uid: String) {
        realtimeDb.child("sos_alerts").child(uid).child("status").setValue("RESOLVED").await()
    }

    suspend fun sendSafeNotification(contacts: List<EmergencyContact>) {
        try {
            val fcmData = mapOf(
                "title" to "✅ All Clear",
                "body" to "${auth.currentUser?.displayName ?: "Family Member"} is safe now",
                "tokens" to contacts.map { it.fcmToken }
            )
            firestore.collection("fcm_queue").add(fcmData).await()
        } catch (e: Exception) { /* ignore */ }
    }

    suspend fun sendZoneExitNotification(contacts: List<EmergencyContact>, zoneName: String) {
        try {
            val fcmData = mapOf(
                "title" to "⚠️ Zone Alert",
                "body" to "${auth.currentUser?.displayName ?: "User"} left $zoneName",
                "tokens" to contacts.map { it.fcmToken }
            )
            firestore.collection("fcm_queue").add(fcmData).await()
        } catch (e: Exception) { /* ignore */ }
    }

    fun observeSOSAlerts(familyGroupId: String): Flow<List<SOSAlert>> = callbackFlow {
        val listener = realtimeDb.child("sos_alerts")
            .orderByChild("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.children?.mapNotNull {
                    SOSAlert(
                        uid = it.child("uid").getValue(String::class.java) ?: "",
                        senderName = it.child("senderName").getValue(String::class.java) ?: "",
                        senderUid = it.child("senderUid").getValue(String::class.java) ?: "",
                        latitude = it.child("latitude").getValue(Double::class.java) ?: 0.0,
                        longitude = it.child("longitude").getValue(Double::class.java) ?: 0.0,
                        reason = it.child("reason").getValue(String::class.java) ?: "",
                        status = it.child("status").getValue(String::class.java) ?: "ACTIVE",
                        timestamp = it.child("timestamp").getValue(Long::class.java) ?: 0L
                    )
                } ?: emptyList()

                trySend(alerts)
            }

        awaitClose { listener.remove() }
    }
}