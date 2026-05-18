package com.karthik.aegis.data.local.dao

import androidx.room.*
import com.karthik.aegis.model.AlertHistory

@Dao
interface AlertHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertHistory)

    @Delete
    suspend fun delete(alert: AlertHistory)

    @Query("SELECT * FROM alert_history WHERE member_uid = :uid ORDER BY timestamp DESC")
    suspend fun getByMemberId(uid: String): List<AlertHistory>

    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getRange(startTime: Long, endTime: Long): List<AlertHistory>

    @Query("DELETE FROM alert_history WHERE timestamp < :beforeTime")
    suspend fun deleteOlderThan(beforeTime: Long)

    @Query("SELECT COUNT(*) FROM alert_history")
    suspend fun count(): Int
}
