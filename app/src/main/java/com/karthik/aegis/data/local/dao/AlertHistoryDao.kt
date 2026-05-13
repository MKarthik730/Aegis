package com.karthik.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.karthik.aegis.model.AlertHistory

@Dao
interface AlertHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertHistory>)

    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    suspend fun getAll(): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getByType(type: String): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE memberUid = :memberUid ORDER BY timestamp DESC")
    suspend fun getByMember(memberUid: String): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getInRange(start: Long, end: Long): List<AlertHistory>

    @Query("DELETE FROM alert_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM alert_history")
    suspend fun count(): Int
}
