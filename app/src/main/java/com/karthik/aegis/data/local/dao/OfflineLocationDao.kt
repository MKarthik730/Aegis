package com.karthik.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.karthik.aegis.model.TrackedLocation

@Dao
interface OfflineLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: TrackedLocation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<TrackedLocation>)

    @Query("SELECT * FROM tracked_locations WHERE uid = :uid ORDER BY timestamp ASC")
    suspend fun getPendingLocations(uid: String): List<TrackedLocation>

    @Query("DELETE FROM tracked_locations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM tracked_locations WHERE uid = :uid")
    suspend fun deleteAllForUser(uid: String)

    @Query("SELECT COUNT(*) FROM tracked_locations WHERE uid = :uid")
    suspend fun countPending(uid: String): Int
}
