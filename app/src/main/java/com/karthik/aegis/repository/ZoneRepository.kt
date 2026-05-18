package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karthik.aegis.model.SafeZone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZoneRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun observeSafeZones(): Flow<List<SafeZone>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val ref = database.child("safe_zones").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zones = snapshot.children.mapNotNull {
                    it.getValue(SafeZone::class.java)
                }
                trySend(zones)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addSafeZone(zone: SafeZone): String {
        val uid = auth.currentUser?.uid ?: return ""
        val zoneId = zone.id.ifEmpty { database.child("safe_zones").child(uid).push().key ?: return "" }
        
        database.child("safe_zones").child(uid).child(zoneId)
            .setValue(zone.copy(id = zoneId)).await()

        return zoneId
    }

    suspend fun removeSafeZone(zoneId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.child("safe_zones").child(uid).child(zoneId).removeValue().await()
    }

    suspend fun updateSafeZone(zone: SafeZone) {
        val uid = auth.currentUser?.uid ?: return
        database.child("safe_zones").child(uid).child(zone.id).setValue(zone).await()
    }

    suspend fun getSafeZones(): List<SafeZone> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = database.child("safe_zones").child(uid).get().await()
            snapshot.children.mapNotNull { it.getValue(SafeZone::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
