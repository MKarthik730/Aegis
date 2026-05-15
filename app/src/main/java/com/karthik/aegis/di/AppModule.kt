package com.karthik.aegis.di

import android.content.Context
import androidx.room.Room
import com.karthik.aegis.data.local.AppDatabase
import com.karthik.aegis.data.local.dao.AlertHistoryDao
import com.karthik.aegis.data.local.dao.OfflineLocationDao
import com.karthik.aegis.data.local.dao.SafetyScoreDao
import com.karthik.aegis.utils.AegisPrefs
import com.karthik.aegis.utils.NotificationUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aegis_database"
        ).build()
    }

    @Provides
    fun provideOfflineLocationDao(db: AppDatabase): OfflineLocationDao {
        return db.offlineLocationDao()
    }

    @Provides
    fun provideAlertHistoryDao(db: AppDatabase): AlertHistoryDao {
        return db.alertHistoryDao()
    }

    @Provides
    fun provideSafetyScoreDao(db: AppDatabase): SafetyScoreDao {
        return db.safetyScoreDao()
    }

    @Provides
    @Singleton
    fun provideAegisPrefs(@ApplicationContext context: Context): AegisPrefs {
        return AegisPrefs(context)
    }

    @Provides
    @Singleton
    fun provideNotificationUtils(): NotificationUtils {
        return NotificationUtils
    }
}