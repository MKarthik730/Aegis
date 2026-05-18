package com.karthik.aegis.data.local.dao

import androidx.room.*
import com.karthik.aegis.model.TrackedLocation

@Dao
interface OfflineLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: TrackedLocation)

    @Delete
    suspend fun delete(location: TrackedLocation)

    @Query("SELECT * FROM tracked_locations WHERE uid = :uid ORDER BY timestamp DESC")
    suspend fun getAllByUid(uid: String): List<TrackedLocation>

    @Query("SELECT * FROM tracked_locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): TrackedLocation?

    @Query("DELETE FROM tracked_locations WHERE timestamp < :beforeTime")
    suspend fun deleteOlderThan(beforeTime: Long)

    @Query("SELECT COUNT(*) FROM tracked_locations WHERE uid = :uid")
    suspend fun countByUid(uid: String): Int
}
