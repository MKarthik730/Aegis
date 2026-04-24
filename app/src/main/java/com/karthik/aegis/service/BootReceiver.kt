package com.karthik.aegis.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: AegisPrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            if (prefs.isLocationTrackingEnabled()) {
                LocationTrackingService.startTracking(context)
            }
            if (prefs.isAccidentDetectionEnabled()) {
                AccidentDetectorService.start(context)
            }
        }
    }
}