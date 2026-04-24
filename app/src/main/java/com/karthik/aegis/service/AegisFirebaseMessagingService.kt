package com.karthik.aegis.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karthik.aegis.utils.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AegisFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store token in Firebase for targeted notifications
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "Aegis Alert"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val alertUid = message.data["alert_uid"] ?: ""
        val senderName = message.data["sender_name"] ?: "Family Member"
        val mapsUrl = message.data["maps_url"] ?: ""

        if (alertUid.isNotEmpty()) {
            NotificationUtils.showSOSIncomingAlert(
                context = this,
                title = title,
                body = body,
                mapsUrl = mapsUrl,
                alertUid = alertUid,
                senderName = senderName
            )
        } else {
            NotificationUtils.showAlert(this, title, body)
        }
    }
}