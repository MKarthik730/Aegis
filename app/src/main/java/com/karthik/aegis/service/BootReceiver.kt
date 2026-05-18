package com.karthik.aegis.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject lateinit var prefs: AegisPrefs

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        context ?: return

        Log.i(TAG, "Device booted — restarting Aegis services")

        // Restart location tracking if it was enabled
        if (prefs.isLocationTrackingEnabled()) {
            LocationTrackingService.startTracking(context)
            Log.d(TAG, "Location tracking restarted")
        }

        // Restart accident detector if it was enabled
        if (prefs.isAccidentDetectionEnabled()) {
            AccidentDetectorService.start(context)
            Log.d(TAG, "Accident detector restarted")
        }
    }
}
