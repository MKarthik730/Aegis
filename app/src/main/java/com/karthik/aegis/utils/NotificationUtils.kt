package com.karthik.aegis.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.karthik.aegis.R
import com.karthik.aegis.ui.MainActivity

object NotificationUtils {

    const val CHANNEL_SOS         = "aegis_sos_channel"
    const val CHANNEL_LOCATION    = "aegis_location_channel"
    const val CHANNEL_ALERTS      = "aegis_alerts_channel"
    const val CHANNEL_FAMILY      = "aegis_family_channel"
    const val CHANNEL_DETECTION   = "aegis_detection_channel"

    fun createNotificationChannels(context: Context) {
        val channels = listOf(
            NotificationChannel(CHANNEL_SOS, "SOS Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Emergency SOS alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            },
            NotificationChannel(CHANNEL_LOCATION, "Location Tracking", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Background location tracking"
            },
            NotificationChannel(CHANNEL_ALERTS, "Safety Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Zone alerts, speed alerts, and anomaly notifications"
                enableVibration(true)
            },
            NotificationChannel(CHANNEL_FAMILY, "Family Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Family member status updates"
            },
            NotificationChannel(CHANNEL_DETECTION, "Detection", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Accident and fall detection service"
            }
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { nm.createNotificationChannel(it) }
    }

    fun showAlert(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_ALERTS,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showSOSIncomingAlert(
        context: Context,
        title: String,
        body: String,
        mapsUrl: String,
        alertUid: String,
        senderName: String
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return

        val openMapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val mapsPendingIntent = PendingIntent.getActivity(
            context, 1, openMapsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alert_uid", alertUid)
            putExtra("sender_name", senderName)
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SOS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to view location"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .addAction(R.drawable.ic_notification, "Open Maps", mapsPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(alertUid.hashCode(), notification)
        vibrateSOS(context)
    }

    fun vibrateSOS(context: Context) {
        val pattern = longArrayOf(
            0,
            200, 100, 200, 100, 200,
            400,
            600, 100, 600, 100, 600,
            400,
            200, 100, 200, 100, 200
        )

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}