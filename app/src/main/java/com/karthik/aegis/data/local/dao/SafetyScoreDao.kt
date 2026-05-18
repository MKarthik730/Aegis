package com.karthik.aegis.data.local.dao

import androidx.room.*
import com.karthik.aegis.model.SafetyScore

@Dao
interface SafetyScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: SafetyScore)

    @Delete
    suspend fun delete(score: SafetyScore)

    @Query("SELECT * FROM safety_scores WHERE uid = :uid AND week_start <= :timestamp AND week_end >= :timestamp")
    suspend fun getCurrentWeek(uid: String, timestamp: Long): SafetyScore?

    @Query("SELECT * FROM safety_scores WHERE uid = :uid ORDER BY week_start DESC LIMIT :limit")
    suspend fun getRecent(uid: String, limit: Int): List<SafetyScore>

    @Query("SELECT * FROM safety_scores WHERE uid = :uid AND week_start BETWEEN :startTime AND :endTime")
    suspend fun getRange(uid: String, startTime: Long, endTime: Long): List<SafetyScore>

    @Query("UPDATE safety_scores SET score = :score WHERE id = :id")
    suspend fun updateScore(id: Long, score: Int)

    @Query("UPDATE safety_scores SET sos_triggers = :count WHERE uid = :uid AND week_start <= :timestamp AND week_end >= :timestamp")
    suspend fun incrementSOSTriggers(uid: String, timestamp: Long, count: Int)
}
