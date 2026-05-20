package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karthik.aegis.data.local.dao.OfflineLocationDao
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.TrackedLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val offlineLocationDao: OfflineLocationDao,
    private val familyRepository: FamilyRepository
) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun queueOfflineLocation(location: TrackedLocation) {
        offlineLocationDao.insert(location)
    }

    suspend fun flushOfflineQueue(uid: String) {
        val queued = offlineLocationDao.getAllByUid(uid)
        queued.forEach { location ->
            try {
                // Store location under /latest (single node, overwritten each time)
                database.child("live_locations").child(uid)
                    .child("latest").setValue(location).await()
                offlineLocationDao.delete(location)
            } catch (e: Exception) {
                // Keep in queue on failure
            }
        }
    }

    fun observeFamilyLocations(familyGroupId: String): Flow<Map<String, TrackedLocation>> = callbackFlow {
        val locRef = database.child("live_locations")
        val membersRef = database.child("family_groups").child(familyGroupId).child("members")
        var locListener: ValueEventListener? = null

        val memberListener = object : ValueEventListener {
            override fun onDataChange(memberSnapshot: DataSnapshot) {
                val memberUids = memberSnapshot.children.mapNotNull { it.key }

                // Remove old location listener if exists (e.g. when members change)
                locListener?.let { locRef.removeEventListener(it) }

                locListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val locations = mutableMapOf<String, TrackedLocation>()
                        snapshot.children.forEach { userSnapshot ->
                            val uid = userSnapshot.key ?: return@forEach
                            if (uid !in memberUids) return@forEach
                            val location = userSnapshot.child("latest")
                                .getValue(TrackedLocation::class.java) ?: return@forEach
                            locations[uid] = location
                        }
                        trySend(locations)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                }
                locRef.addValueEventListener(locListener!!)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        membersRef.addValueEventListener(memberListener)

        awaitClose {
            membersRef.removeEventListener(memberListener)
            locListener?.let { locRef.removeEventListener(it) }
        }
    }

    suspend fun getUserLocation(uid: String): TrackedLocation? {
        return try {
            val snapshot = database.child("live_locations").child(uid).child("latest").get().await()
            snapshot.getValue(TrackedLocation::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
