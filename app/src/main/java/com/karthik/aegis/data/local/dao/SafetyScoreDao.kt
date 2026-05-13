package com.karthik.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.karthik.aegis.model.SafetyScore

@Dao
interface SafetyScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: SafetyScore)

    @Query("SELECT * FROM safety_scores WHERE uid = :uid ORDER BY weekStart DESC LIMIT 1")
    suspend fun getLatest(uid: String): SafetyScore?

    @Query("SELECT * FROM safety_scores WHERE uid = :uid ORDER BY weekStart DESC")
    suspend fun getAllForUser(uid: String): List<SafetyScore>

    @Query("SELECT * FROM safety_scores WHERE uid = :uid AND weekStart = :weekStart LIMIT 1")
    suspend fun getForWeek(uid: String, weekStart: Long): SafetyScore?

    @Query("SELECT * FROM safety_scores WHERE uid IN (:uids) AND weekStart = :weekStart")
    suspend fun getForFamily(uids: List<String>, weekStart: Long): List<SafetyScore>

    @Query("DELETE FROM safety_scores WHERE uid = :uid")
    suspend fun deleteAllForUser(uid: String)
}
