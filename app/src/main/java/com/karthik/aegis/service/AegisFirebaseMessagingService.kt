package com.karthik.aegis.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.karthik.aegis.utils.AegisPrefs
import com.karthik.aegis.utils.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AegisFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var prefs: AegisPrefs

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            prefs.setFcmToken(token)
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().reference
                    .child("users").child(uid).child("fcmToken")
                    .setValue(token)
            }
        }
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