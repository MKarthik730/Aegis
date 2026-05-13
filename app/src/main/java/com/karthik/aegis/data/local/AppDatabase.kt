package com.karthik.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.karthik.aegis.data.local.dao.AlertHistoryDao
import com.karthik.aegis.data.local.dao.OfflineLocationDao
import com.karthik.aegis.data.local.dao.SafetyScoreDao
import com.karthik.aegis.model.AlertHistory
import com.karthik.aegis.model.CheckIn
import com.karthik.aegis.model.SafetyScore
import com.karthik.aegis.model.TrackedLocation

@Database(
    entities = [
        TrackedLocation::class,
        AlertHistory::class,
        SafetyScore::class,
        CheckIn::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun offlineLocationDao(): OfflineLocationDao
    abstract fun alertHistoryDao(): AlertHistoryDao
    abstract fun safetyScoreDao(): SafetyScoreDao
}
