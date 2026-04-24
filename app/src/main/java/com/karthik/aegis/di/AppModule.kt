package com.karthik.aegis.di

import android.content.Context
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
    fun provideAegisPrefs(@ApplicationContext context: Context): AegisPrefs {
        return AegisPrefs(context)
    }

    @Provides
    @Singleton
    fun provideNotificationUtils(): NotificationUtils {
        return NotificationUtils
    }
}