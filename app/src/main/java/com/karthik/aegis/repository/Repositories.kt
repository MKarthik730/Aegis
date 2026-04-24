package com.karthik.aegis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.model.TrackedLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getLiveLocation(uid: String): TrackedLocation? {
        return try {
            val snapshot = database.child("live_locations").child(uid).get().await()
            snapshot.getValue(TrackedLocation::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun observeLiveLocation(uid: String): Flow<TrackedLocation?> = callbackFlow {
        val listener = database.child("live_locations").child(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.getValue(TrackedLocation::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun flushOfflineQueue(uid: String) {
        // Room DB queue flush would happen here
        // For now, this is a placeholder
    }

    suspend fun queueOfflineLocation(location: TrackedLocation) {
        // Room DB queue insert would happen here
        // For now, this is a placeholder
    }

    fun observeFamilyLocations(groupId: String): Flow<Map<String, TrackedLocation>> = callbackFlow {
        val listener = database.child("live_locations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val locations = snapshot?.children?.associate { child ->
                    child.key to (child.getValue(TrackedLocation::class.java) ?: TrackedLocation(uid = child.key ?: ""))
                } ?: emptyMap()
                trySend(locations)
            }
        awaitClose { listener.remove() }
    }
}

@Singleton
class FamilyRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun createFamilyGroup(name: String): String {
        val uid = auth.currentUser?.uid ?: return ""
        val groupId = database.child("family_groups").push().key ?: return ""
        val member = FamilyMember(
            uid = uid,
            name = auth.currentUser?.displayName ?: "User",
            email = auth.currentUser?.email ?: "",
            phone = auth.currentUser?.phoneNumber ?: "",
            photoUrl = auth.currentUser?.photoUrl?.toString() ?: "",
            role = "ADMIN",
            status = "SAFE"
        )
        database.child("family_groups").child(groupId).child("members").child(uid).setValue(member).await()
        database.child("users").child(uid).child("familyGroupId").setValue(groupId).await()
        return groupId
    }

    suspend fun joinFamilyGroup(groupId: String) {
        val uid = auth.currentUser?.uid ?: return
        val member = FamilyMember(
            uid = uid,
            name = auth.currentUser?.displayName ?: "User",
            email = auth.currentUser?.email ?: "",
            phone = auth.currentUser?.phoneNumber ?: "",
            photoUrl = auth.currentUser?.photoUrl?.toString() ?: "",
            role = "MEMBER",
            status = "SAFE"
        )
        database.child("family_groups").child(groupId).child("members").child(uid).setValue(member).await()
        database.child("users").child(uid).child("familyGroupId").setValue(groupId).await()
    }

    fun observeFamilyMembers(groupId: String): Flow<List<FamilyMember>> = callbackFlow {
        val listener = database.child("family_groups").child(groupId).child("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.children?.mapNotNull {
                    it.getValue(FamilyMember::class.java)
                } ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }
}

@Singleton
class ZoneRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getSafeZones(): List<SafeZone> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = database.child("safe_zones").child(uid).get().await()
            snapshot.children.mapNotNull {
                SafeZone(
                    id = it.key ?: "",
                    name = it.child("name").getValue(String::class.java) ?: "",
                    latitude = it.child("latitude").getValue(Double::class.java) ?: 0.0,
                    longitude = it.child("longitude").getValue(Double::class.java) ?: 0.0,
                    radiusMeters = it.child("radiusMeters").getValue(Double::class.java) ?: 150.0,
                    type = it.child("type").getValue(String::class.java) ?: "SAFE",
                    isHome = it.child("isHome").getValue(Boolean::class.java) ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSafeZone(zone: SafeZone) {
        val uid = auth.currentUser?.uid ?: return
        val zoneId = zone.id.ifEmpty { database.child("safe_zones").child(uid).push().key ?: return }
        database.child("safe_zones").child(uid).child(zoneId).setValue(zone.copy(id = zoneId)).await()
    }

    suspend fun removeSafeZone(zoneId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.child("safe_zones").child(uid).child(zoneId).removeValue().await()
    }

    fun observeSafeZones(): Flow<List<SafeZone>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { close(); return@callbackFlow }
        val listener = database.child("safe_zones").child(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val zones = snapshot?.children?.mapNotNull {
                    SafeZone(
                        id = it.key ?: "",
                        name = it.child("name").getValue(String::class.java) ?: "",
                        latitude = it.child("latitude").getValue(Double::class.java) ?: 0.0,
                        longitude = it.child("longitude").getValue(Double::class.java) ?: 0.0,
                        radiusMeters = it.child("radiusMeters").getValue(Double::class.java) ?: 150.0,
                        type = it.child("type").getValue(String::class.java) ?: "SAFE",
                        isHome = it.child("isHome").getValue(Boolean::class.java) ?: false
                    )
                } ?: emptyList()
                trySend(zones)
            }
        awaitClose { listener.remove() }
    }
}