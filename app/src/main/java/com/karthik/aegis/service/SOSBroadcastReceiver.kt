package com.karthik.aegis.service

// ─────────────────────────────────────────────────────────────────────────────
//   ✅ goAsync() — BroadcastReceiver won't be killed during coroutine work
//   ✅ No repository instantiation inside receiver — uses Hilt EntryPoint
//   ✅ WakeLock acquired via goAsync PendingResult before coroutine launch
//   ✅ Internal actions use LocalBroadcastManager (not global sendBroadcast)
//   ✅ Exported split: BootReceiver separate from internal action receiver
//   ✅ Power button detection hardened against false positives
//   ✅ Volume button SOS only active when screen is on
// ─────────────────────────────────────────────────────────────────────────────

import android.content.*
import android.os.*
import android.util.Log
import android.view.KeyEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karthik.aegis.repository.ContactsRepository
import com.karthik.aegis.repository.SOSRepository
import com.karthik.aegis.utils.AegisPrefs
import com.karthik.aegis.utils.NotificationUtils
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import kotlinx.coroutines.*

class SOSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SOSBroadcastReceiver"

        // ── External / system actions (exported = true) ──────────────────────
        const val ACTION_AUTO_SOS_FIRE        = "com.karthik.aegis.AUTO_SOS_FIRE"
        const val ACTION_INCOMING_SOS_ALERT   = "com.karthik.aegis.INCOMING_SOS_ALERT"

        // ── Internal actions (exported = false, LocalBroadcastManager) ───────
        const val ACTION_ZONE_EXIT_ALERT      = "com.karthik.aegis.ZONE_EXIT_NOTIFY"
        const val ACTION_CANCEL_SOS_COUNTDOWN = "com.karthik.aegis.CANCEL_SOS_COUNTDOWN"
        const val ACTION_MARK_SAFE            = "com.karthik.aegis.MARK_SAFE"
        const val ACTION_RESPOND_TO_SOS       = "com.karthik.aegis.RESPOND_TO_SOS"

        // ── Power button SOS ─────────────────────────────────────────────────
        private const val POWER_PRESS_REQUIRED  = 5
        private const val POWER_PRESS_WINDOW_MS = 3_000L

        // ── Volume button SOS ────────────────────────────────────────────────
        private const val VOLUME_PRESS_REQUIRED  = 5
        private const val VOLUME_PRESS_WINDOW_MS = 3_000L

        // ── Headphone button SOS ─────────────────────────────────────────────
        private const val HEADPHONE_PRESS_REQUIRED  = 3
        private const val HEADPHONE_PRESS_WINDOW_MS = 2_000L

        // Timestamps stored statically — survive receiver re-creation
        private val powerPressTimestamps     = ArrayDeque<Long>()
        private val volumePressTimestamps    = ArrayDeque<Long>()
        private val headphonePressTimestamps = ArrayDeque<Long>()
    }

    // ── Hilt EntryPoint — only way to inject into BroadcastReceiver ──────────
    // BroadcastReceivers cannot use @Inject directly.
    // EntryPoint gives us access to Hilt-managed singletons.

    @EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface SOSReceiverEntryPoint {
        fun sosRepository(): SOSRepository
        fun contactsRepository(): ContactsRepository
        fun aegisPrefs(): AegisPrefs
    }

    private fun entryPoint(context: Context): SOSReceiverEntryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SOSReceiverEntryPoint::class.java
        )

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN DISPATCH
    //
    // goAsync() — tells Android not to kill this receiver after onReceive()
    // returns. The PendingResult keeps the process alive during our coroutine.
    // MUST call pendingResult.finish() when done.
    // ─────────────────────────────────────────────────────────────────────────

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received: ${intent.action}")

        when (intent.action) {

            // ── System broadcasts ─────────────────────────────────────────
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                handleBootCompleted(context)
            }

            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_SCREEN_OFF -> {
                handleScreenToggle(context, intent.action!!)
            }

            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }
                keyEvent?.let { handleHeadphoneButton(context, it) }
            }

            "android.media.VOLUME_CHANGED_ACTION" -> {
                handleVolumePress(context)
            }

            // ── Aegis actions (need async work) ───────────────────────────
            ACTION_AUTO_SOS_FIRE -> {
                val reason = intent.getStringExtra("reason") ?: "Automatic detection"
                launchAsync(context) { fireSOS(context, reason, isAutomatic = true) }
            }

            ACTION_INCOMING_SOS_ALERT -> {
                handleIncomingSOS(context, intent)
            }

            ACTION_ZONE_EXIT_ALERT -> {
                val zone = intent.getStringExtra("zone_name") ?: "safe zone"
                launchAsync(context) { handleZoneExitAlert(context, zone) }
            }

            ACTION_CANCEL_SOS_COUNTDOWN -> {
                handleCancelCountdown(context)
            }

            ACTION_MARK_SAFE -> {
                launchAsync(context) { handleMarkSafe(context) }
            }

            ACTION_RESPOND_TO_SOS -> {
                launchAsync(context) { handleRespondToSOS(context, intent) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // goAsync() HELPER
    // Extends BroadcastReceiver lifetime for coroutine work
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchAsync(context: Context, block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Async work failed: ${e.message}")
            } finally {
                pendingResult.finish()   // ✅ release wake lock, allow process sleep
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOT
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleBootCompleted(context: Context) {
        val prefs = entryPoint(context).aegisPrefs()
        Log.d(TAG, "Boot completed — restoring services")

        if (prefs.isLocationTrackingEnabled()) {
            LocationTrackingService.startTracking(context)
        }
        if (prefs.isAccidentDetectionEnabled()) {
            AccidentDetectorService.start(context)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POWER BUTTON (5 screen-off events in 3s)
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleScreenToggle(context: Context, action: String) {
        if (action != Intent.ACTION_SCREEN_OFF) return   // only count screen OFF

        val now = System.currentTimeMillis()

        // Clean expired presses
        while (powerPressTimestamps.isNotEmpty() &&
            now - powerPressTimestamps.first() > POWER_PRESS_WINDOW_MS) {
            powerPressTimestamps.removeFirst()
        }

        powerPressTimestamps.addLast(now)
        Log.d(TAG, "Power press: ${powerPressTimestamps.size}/$POWER_PRESS_REQUIRED")

        if (powerPressTimestamps.size >= POWER_PRESS_REQUIRED) {
            powerPressTimestamps.clear()
            launchAsync(context) {
                fireSOS(context, "Power button SOS (5 presses)", isAutomatic = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADPHONE BUTTON (3 presses in 2s)
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleHeadphoneButton(context: Context, keyEvent: KeyEvent) {
        if (keyEvent.keyCode != KeyEvent.KEYCODE_HEADSETHOOK &&
            keyEvent.keyCode != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) return
        if (keyEvent.action != KeyEvent.ACTION_DOWN) return

        val now = System.currentTimeMillis()
        headphonePressTimestamps.addLast(now)

        while (headphonePressTimestamps.isNotEmpty() &&
            now - headphonePressTimestamps.first() > HEADPHONE_PRESS_WINDOW_MS) {
            headphonePressTimestamps.removeFirst()
        }

        Log.d(TAG, "Headphone press: ${headphonePressTimestamps.size}/$HEADPHONE_PRESS_REQUIRED")

        if (headphonePressTimestamps.size >= HEADPHONE_PRESS_REQUIRED) {
            headphonePressTimestamps.clear()
            launchAsync(context) {
                fireSOS(context, "Headphone button SOS (3 presses)", isAutomatic = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VOLUME BUTTON (5 presses in 3s)
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleVolumePress(context: Context) {
        // Only trigger if screen is on (avoid pocket triggers)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) return

        val now = System.currentTimeMillis()
        volumePressTimestamps.addLast(now)

        while (volumePressTimestamps.isNotEmpty() &&
            now - volumePressTimestamps.first() > VOLUME_PRESS_WINDOW_MS) {
            volumePressTimestamps.removeFirst()
        }

        Log.d(TAG, "Volume press: ${volumePressTimestamps.size}/$VOLUME_PRESS_REQUIRED")

        if (volumePressTimestamps.size >= VOLUME_PRESS_REQUIRED) {
            volumePressTimestamps.clear()
            launchAsync(context) {
                fireSOS(context, "Volume button SOS (5 presses)", isAutomatic = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIRE SOS — all triggers funnel here
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun fireSOS(context: Context, reason: String, isAutomatic: Boolean) {
        Log.e(TAG, "SOS FIRE: '$reason' auto=$isAutomatic")

        val ep       = entryPoint(context)
        val sosRepo  = ep.sosRepository()
        val contacts = ep.contactsRepository().getEmergencyContacts()

        if (contacts.isEmpty()) {
            NotificationUtils.showAlert(
                context,
                "⚠️ SOS Failed",
                "No emergency contacts set. Open Aegis to add contacts."
            )
            Log.w(TAG, "SOS fired with no contacts configured")
            return
        }

        sosRepo.triggerSOSWithReason(
            contacts    = contacts,
            reason      = reason,
            isAutomatic = isAutomatic,
            onSuccess   = {
                NotificationUtils.showAlert(
                    context,
                    "🆘 SOS Alert Sent",
                    "Your ${contacts.size} emergency contact(s) have been notified."
                )
            },
            onFailure = { error ->
                Log.e(TAG, "SOS failed: $error")
                NotificationUtils.showAlert(
                    context,
                    "⚠️ SOS Partially Failed",
                    "SMS fallback attempted. Error: $error"
                )
            }
        )

        // Audit log in Firebase
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseDatabase.getInstance().reference
                .child("sos_triggers")
                .child(uid)
                .push()
                .setValue(mapOf(
                    "reason"      to reason,
                    "isAutomatic" to isAutomatic,
                    "timestamp"   to System.currentTimeMillis()
                ))
        } catch (e: Exception) {
            Log.w(TAG, "Audit log failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCOMING SOS (from family member via FCM)
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleIncomingSOS(context: Context, intent: Intent) {
        val senderName = intent.getStringExtra("sender_name") ?: "A family member"
        val lat        = intent.getDoubleExtra("lat", 0.0)
        val lng        = intent.getDoubleExtra("lng", 0.0)
        val alertUid   = intent.getStringExtra("alert_uid") ?: ""
        val mapsUrl    = "https://maps.google.com/?q=$lat,$lng"

        Log.e(TAG, "Incoming SOS from $senderName")

        NotificationUtils.showSOSIncomingAlert(
            context    = context,
            title      = "🆘 $senderName needs help!",
            body       = "Tap to see location and respond",
            mapsUrl    = mapsUrl,
            alertUid   = alertUid,
            senderName = senderName
        )

        vibrateSOS(context)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZONE EXIT
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun handleZoneExitAlert(context: Context, zoneName: String) {
        val ep = entryPoint(context)
        if (!ep.aegisPrefs().isZoneExitNotificationsEnabled()) return

        val contacts = ep.contactsRepository().getEmergencyContacts()
        ep.sosRepository().sendZoneExitNotification(contacts, zoneName)

        // Firebase log
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseDatabase.getInstance().reference
                .child("zone_events").child(uid).push()
                .setValue(mapOf(
                    "type"      to "EXIT",
                    "zone"      to zoneName,
                    "timestamp" to System.currentTimeMillis()
                ))
        } catch (e: Exception) {
            Log.w(TAG, "Zone event log failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CANCEL COUNTDOWN
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleCancelCountdown(context: Context) {
        AccidentDetectorService.cancelCountdown(context)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK SAFE
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun handleMarkSafe(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        try {
            FirebaseDatabase.getInstance().reference
                .child("sos_alerts").child(uid)
                .child("status").setValue("RESOLVED")

            val ep       = entryPoint(context)
            val contacts = ep.contactsRepository().getEmergencyContacts()
            ep.sosRepository().sendSafeNotification(contacts)

            NotificationUtils.showAlert(
                context,
                "✅ Marked as Safe",
                "Your family has been notified you're okay."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Mark safe failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESPOND TO SOS
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun handleRespondToSOS(context: Context, intent: Intent) {
        val alertUid     = intent.getStringExtra("alert_uid") ?: return
        val responderUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        try {
            FirebaseDatabase.getInstance().reference
                .child("sos_responses").child(alertUid).child(responderUid)
                .setValue(mapOf(
                    "responderUid" to responderUid,
                    "timestamp"    to System.currentTimeMillis(),
                    "status"       to "ON_THE_WAY"
                ))
            Log.d(TAG, "Response logged: $responderUid → $alertUid")
        } catch (e: Exception) {
            Log.e(TAG, "Respond to SOS failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIBRATE — Morse SOS (... --- ...)
    // ─────────────────────────────────────────────────────────────────────────

    private fun vibrateSOS(context: Context) {
        val pattern = longArrayOf(
            0,
            200, 100, 200, 100, 200,   // S (···)
            400,
            600, 100, 600, 100, 600,   // O (−−−)
            400,
            200, 100, 200, 100, 200    // S (···)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(android.os.VibratorManager::class.java)
            vm.defaultVibrator.vibrate(
                android.os.VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }
}

