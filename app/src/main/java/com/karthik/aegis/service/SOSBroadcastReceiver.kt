package com.karthik.aegis.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.karthik.aegis.repository.ContactsRepository
import com.karthik.aegis.repository.SOSRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SOSBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var sosRepository: SOSRepository
    @Inject lateinit var contactsRepository: ContactsRepository

    companion object {
        private const val TAG = "SOSBroadcastReceiver"
        const val ACTION_AUTO_SOS_FIRE = "com.karthik.aegis.AUTO_SOS_FIRE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION_AUTO_SOS_FIRE) return

        Log.w(TAG, "SOS Broadcast received — firing alert")

        val reason = intent.getStringExtra("reason") ?: "Emergency SOS"

        // Run in background scope
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid ?: return@launch

                val contacts = contactsRepository.getEmergencyContacts()
                sosRepository.triggerSOSWithReason(
                    contacts = contacts,
                    reason = reason,
                    isAutomatic = true,
                    latitude = 0.0,
                    longitude = 0.0,
                    onSuccess = {
                        Log.i(TAG, "SOS sent successfully")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "SOS failed: $error")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "SOS error: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
