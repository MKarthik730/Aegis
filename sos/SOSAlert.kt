package com.karthik.aegis.sos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

// ─────────────────────────────────────────
// MODEL
// ─────────────────────────────────────────

data class SOSAlert(
    val uid: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val message: String = "🆘 SOS! I need help!",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE" // ACTIVE, RESOLVED
)

data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val fcmToken: String = ""
)

// ─────────────────────────────────────────
// SOS MANAGER
// ─────────────────────────────────────────

class SOSManager(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    /**
     * Main function — call this on SOS button tap
     * 1. Gets current location
     * 2. Saves alert to Firebase
     * 3. Sends SMS to emergency contacts
     * 4. Sends FCM push notification
     */
    fun triggerSOS(
        contacts: List<EmergencyContact>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onFailure("User not logged in")
            return
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onFailure("Location permission not granted")
            return
        }

        // Get current location then trigger SOS
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val alert = SOSAlert(
                        uid = uid,
                        name = auth.currentUser?.displayName ?: "Family Member",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        message = "🆘 SOS! I need help! My location: " +
                                "https://maps.google.com/?q=${location.latitude},${location.longitude}",
                        timestamp = System.currentTimeMillis(),
                        status = "ACTIVE"
                    )

                    // Save to Firebase
                    saveAlertToFirebase(alert)

                    // Send SMS fallback
                    sendSMSToContacts(contacts, alert)

                    // Send FCM push notification
                    sendFCMAlert(contacts, alert)

                    onSuccess()
                } else {
                    onFailure("Could not get location. Try again.")
                }
            }
            .addOnFailureListener {
                onFailure("Location error: ${it.message}")
            }
    }

    // ─────────────────────────────────────
    // FIREBASE
    // ─────────────────────────────────────

    private fun saveAlertToFirebase(alert: SOSAlert) {
        val uid = auth.currentUser?.uid ?: return
        database
            .child("sos_alerts")
            .child(uid)
            .setValue(alert)
    }

    fun resolveSOSAlert() {
        val uid = auth.currentUser?.uid ?: return
        database
            .child("sos_alerts")
            .child(uid)
            .child("status")
            .setValue("RESOLVED")
    }

    // ─────────────────────────────────────
    // SMS FALLBACK
    // ─────────────────────────────────────

    private fun sendSMSToContacts(
        contacts: List<EmergencyContact>,
        alert: SOSAlert
    ) {
        val smsManager = SmsManager.getDefault()
        contacts.forEach { contact ->
            try {
                smsManager.sendTextMessage(
                    contact.phone,
                    null,
                    alert.message,
                    null,
                    null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─────────────────────────────────────
    // FCM PUSH NOTIFICATION
    // ─────────────────────────────────────

    private fun sendFCMAlert(
        contacts: List<EmergencyContact>,
        alert: SOSAlert
    ) {
        // Send FCM notification to each contact's device token
        // This requires a backend (Firebase Cloud Functions or FastAPI)
        // Store alert in Firestore — Cloud Function triggers FCM automatically
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val notification = hashMapOf(
            "title" to "🆘 SOS Alert!",
            "body" to "${alert.name} needs help!",
            "lat" to alert.latitude,
            "lng" to alert.longitude,
            "timestamp" to alert.timestamp,
            "tokens" to contacts.map { it.fcmToken }
        )
        db.collection("fcm_queue").add(notification)
    }
}

// ─────────────────────────────────────────
// SOS VIEWMODEL
// ─────────────────────────────────────────

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SOSState {
    object Idle : SOSState()
    object Loading : SOSState()
    object Success : SOSState()
    data class Error(val message: String) : SOSState()
}

class SOSViewModel(private val sosManager: SOSManager) : ViewModel() {

    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState

    fun triggerSOS(contacts: List<EmergencyContact>) {
        _sosState.value = SOSState.Loading
        viewModelScope.launch {
            sosManager.triggerSOS(
                contacts = contacts,
                onSuccess = {
                    _sosState.value = SOSState.Success
                },
                onFailure = { error ->
                    _sosState.value = SOSState.Error(error)
                }
            )
        }
    }

    fun resolveSOS() {
        sosManager.resolveSOSAlert()
        _sosState.value = SOSState.Idle
    }
}

// ─────────────────────────────────────────
// SOS SCREEN (Jetpack Compose)
// ─────────────────────────────────────────

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SOSScreen(viewModel: SOSViewModel, contacts: List<EmergencyContact>) {

    val sosState by viewModel.sosState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Confirm dialog before firing SOS
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Send SOS Alert?") },
            text = { Text("This will alert all your emergency contacts with your live location.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.triggerSOS(contacts)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Send SOS", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // SOS Button
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = sosState !is SOSState.Loading
            ) {
                Text(
                    text = if (sosState is SOSState.Loading) "Sending..." else "SOS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status message
            when (sosState) {
                is SOSState.Success -> {
                    Text(
                        text = "✅ Alert sent to your family!",
                        color = Color.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.resolveSOS() }) {
                        Text("I'm Safe Now")
                    }
                }
                is SOSState.Error -> {
                    Text(
                        text = "❌ ${(sosState as SOSState.Error).message}",
                        color = Color.Red
                    )
                }
                else -> {
                    Text(
                        text = "Press and hold if you're in danger",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
