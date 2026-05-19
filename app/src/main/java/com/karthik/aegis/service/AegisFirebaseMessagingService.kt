package com.karthik.aegis.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karthik.aegis.R
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AegisFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var aegisPrefs: AegisPrefs

    companion object {
        private const val TAG = "AegisMessaging"
        private const val CHANNEL_ID = "aegis_fcm_channel"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM message received from: ${message.from}")

        // Handle data messages
        if (message.data.isNotEmpty()) {
            val title = message.data["title"] ?: "Aegis Alert"
            val body = message.data["body"] ?: ""
            val alertType = message.data["type"] ?: "general"

            showNotification(title, body, alertType)
        }

        // Handle notification messages
        message.notification?.let {
            showNotification(it.title ?: "Aegis", it.body ?: "", "notification")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed: $token")
        // Save token to Firebase or SharedPreferences for later use
        saveFCMToken(token)
    }

    private fun showNotification(title: String, body: String, alertType: String) {
        createNotificationChannel()

        val intent = Intent(this, Class.forName("com.karthik.aegis.ui.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alert_type", alertType)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d(TAG, "Notification shown: $title — $body")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aegis Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveFCMToken(token: String) {
        aegisPrefs.setUserFCMToken(token)
        Log.d(TAG, "FCM token saved to preferences")
    }
}
