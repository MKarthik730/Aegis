package com.karthik.aegis.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.karthik.aegis.service.AccidentDetectorService
import com.karthik.aegis.service.LocationTrackingService
import com.karthik.aegis.ui.navigation.AegisNavHost
import com.karthik.aegis.ui.theme.AegisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val auth = FirebaseAuth.getInstance()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, isGranted) ->
            Log.d(TAG, "Permission $permission: ${if (isGranted) "GRANTED" else "DENIED"}")
        }

        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationTracking()
        }

        if (permissions[Manifest.permission.CAMERA] == true) {
            startAccidentDetection()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Splash Screen before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestRequiredPermissions()

        // Get/Update FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")
                // TODO: Save to Firebase or prefs
            }
        }

        setContent {
            AegisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AegisNavHost()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        
        // Check if user is logged in
        if (auth.currentUser != null) {
            // Start services if user is logged in
            startLocationTracking()
            startAccidentDetection()
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        // Add background location for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startLocationTracking() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            LocationTrackingService.startTracking(this, LocationTrackingService.MODE_PASSIVE)
            Log.d(TAG, "Location tracking started")
        }
    }

    private fun startAccidentDetection() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            AccidentDetectorService.start(this)
            Log.d(TAG, "Accident detection started")
        }
    }
}
