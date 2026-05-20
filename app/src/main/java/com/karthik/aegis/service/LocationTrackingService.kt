package com.karthik.aegis.service

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karthik.aegis.R
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.repository.LocationRepository
import com.karthik.aegis.ui.MainActivity
import com.karthik.aegis.utils.AegisPrefs
import com.karthik.aegis.utils.DistanceUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    companion object {
        private const val TAG = "LocationTrackingService"
        const val CHANNEL_ID          = "aegis_location_channel"
        const val NOTIFICATION_ID     = 1001

        const val ACTION_LOCATION_UPDATE  = "com.karthik.aegis.LOCATION_UPDATE"
        const val ACTION_ZONE_ENTERED     = "com.karthik.aegis.ZONE_ENTERED"
        const val ACTION_ZONE_EXITED      = "com.karthik.aegis.ZONE_EXITED"
        const val ACTION_ANOMALY_DETECTED = "com.karthik.aegis.ANOMALY_DETECTED"
        const val ACTION_HOME_ARRIVED     = "com.karthik.aegis.HOME_ARRIVED"
        const val ACTION_ROUTE_DEVIATION  = "com.karthik.aegis.ROUTE_DEVIATION"

        const val EXTRA_LATITUDE    = "lat"
        const val EXTRA_LONGITUDE   = "lng"
        const val EXTRA_SPEED       = "speed"
        const val EXTRA_ZONE_NAME   = "zone_name"
        const val EXTRA_ANOMALY_MSG = "anomaly_msg"

        const val MODE_ACTIVE  = "ACTIVE"
        const val MODE_PASSIVE = "PASSIVE"

        private val FIREBASE_THROTTLE_MS      = TimeUnit.SECONDS.toMillis(10)
        private val ANOMALY_STATIONARY_MS     = TimeUnit.MINUTES.toMillis(30)
        private val ANOMALY_NO_CHECKIN_MS     = TimeUnit.HOURS.toMillis(2)
        private const val ROUTE_DEVIATION_METERS = 200.0
        private const val SPEED_DRIVING_MPS      = 5.0f
        private const val GEOFENCE_RADIUS_METERS = 150.0

        fun startTracking(context: Context, mode: String = MODE_PASSIVE) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                putExtra("mode", mode)
            }
            context.startForegroundService(intent)
        }

        fun stopTracking(context: Context) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
        }
    }

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var prefs: AegisPrefs

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var handlerThread: HandlerThread

    private val database = FirebaseDatabase.getInstance().reference
    private val auth     = FirebaseAuth.getInstance()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var trackingMode         = MODE_PASSIVE
    private var lastFirebaseSyncMs   = 0L
    private var lastMovementMs       = System.currentTimeMillis()
    private var lastCheckinMs        = System.currentTimeMillis()
    private var lastKnownLat         = 0.0
    private var lastKnownLng         = 0.0
    private var safeZones            = listOf<SafeZone>()
    private var zonesCurrentlyInside = mutableSetOf<String>()
    private var plannedRoutePoints   = listOf<Pair<Double, Double>>()
    private var anomalyCheckJob: Job? = null
    private var safeZoneListener: ValueEventListener? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        notificationManager  = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager  = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        handlerThread = HandlerThread("AegisLocationThread").also { it.start() }
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Aegis::LocationWakeLock")

        createNotificationChannel()
        registerNetworkCallback()
        listenForSafeZoneChanges()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newMode = intent?.getStringExtra("mode") ?: MODE_PASSIVE
        
        if (locationCallback == null || newMode != trackingMode) {
            trackingMode = newMode
            startLocationUpdates()
        }

        val notification = buildNotification("Aegis is protecting you 🛡️")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!wakeLock.isHeld) wakeLock.acquire(TimeUnit.HOURS.toMillis(12))

        startAnomalyMonitor()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        anomalyCheckJob?.cancel()
        serviceScope.cancel()
        handlerThread.quitSafely()

        if (wakeLock.isHeld) wakeLock.release()

        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }

        safeZoneListener?.let {
            val uid = auth.currentUser?.uid ?: return@let
            database.child("safe_zones").child(uid).removeEventListener(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onNewLocation(it) }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            buildLocationRequest(trackingMode),
            locationCallback!!,
            handlerThread.looper
        )
    }

    private fun buildLocationRequest(mode: String): LocationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            if (mode == MODE_ACTIVE) 5_000L else 30_000L
        ).apply {
            setMinUpdateIntervalMillis(if (mode == MODE_ACTIVE) 3_000L else 15_000L)
            setMinUpdateDistanceMeters(if (mode == MODE_ACTIVE) 5f else 20f)
            setMaxUpdateDelayMillis(if (mode == MODE_ACTIVE) 10_000L else 60_000L)
        }.build()

    private fun onNewLocation(location: android.location.Location) {
        val lat   = location.latitude
        val lng   = location.longitude
        val speed = location.speed

        if (speed > 83f) return

        broadcastLocationUpdate(lat, lng, speed)
        syncToFirebaseOrQueue(lat, lng, speed)
        checkSafeZones(lat, lng)
        if (plannedRoutePoints.isNotEmpty()) checkRouteDeviation(lat, lng)
        checkHomeWifi()
        
        val newMode = if (speed > SPEED_DRIVING_MPS) MODE_ACTIVE else MODE_PASSIVE
        if (newMode != trackingMode) {
            trackingMode = newMode
            startLocationUpdates()
        }

        val moved = DistanceUtils.distanceMeters(lastKnownLat, lastKnownLng, lat, lng)
        if (moved > 20) lastMovementMs = System.currentTimeMillis()

        lastKnownLat = lat
        lastKnownLng = lng
    }

    private fun syncToFirebaseOrQueue(lat: Double, lng: Double, speed: Float) {
        val now = System.currentTimeMillis()
        if (now - lastFirebaseSyncMs < FIREBASE_THROTTLE_MS) return

        val uid = auth.currentUser?.uid ?: return
        lastFirebaseSyncMs = now

        val tracked = TrackedLocation(
            uid       = uid,
            latitude  = lat,
            longitude = lng,
            speed     = speed,
            timestamp = now,
            mode      = trackingMode
        )

        serviceScope.launch {
            try {
                if (isNetworkAvailable()) {
                    database.child("live_locations").child(uid).child("latest").setValue(tracked).await()
                    locationRepository.flushOfflineQueue(uid)
                } else {
                    locationRepository.queueOfflineLocation(tracked)
                }
            } catch (e: Exception) {
                locationRepository.queueOfflineLocation(tracked)
            }
        }
    }

    private fun checkRouteDeviation(lat: Double, lng: Double) {
        val minDistance = plannedRoutePoints.minOfOrNull { (rLat, rLng) ->
            DistanceUtils.distanceMeters(lat, lng, rLat, rLng)
        } ?: return

        if (minDistance > ROUTE_DEVIATION_METERS) {
            broadcastLocal(ACTION_ROUTE_DEVIATION) {
                putExtra(EXTRA_LATITUDE, lat)
                putExtra(EXTRA_LONGITUDE, lng)
                putExtra(EXTRA_ANOMALY_MSG, "Deviated from route")
            }
            updateNotification("⚠️ Off your planned route!")
        }
    }

    private fun checkSafeZones(lat: Double, lng: Double) {
        safeZones.forEach { zone ->
            val distance = DistanceUtils.distanceMeters(lat, lng, zone.latitude, zone.longitude)
            val isInside = distance <= (zone.radiusMeters.takeIf { it > 0 } ?: GEOFENCE_RADIUS_METERS)

            if (isInside && !zonesCurrentlyInside.contains(zone.id)) {
                zonesCurrentlyInside.add(zone.id)
                broadcastZoneEvent(ACTION_ZONE_ENTERED, zone.name)
                updateNotification("📍 Entered: ${zone.name}")
                if (zone.isHome) broadcastLocal(ACTION_HOME_ARRIVED)
            } else if (!isInside && zonesCurrentlyInside.contains(zone.id)) {
                zonesCurrentlyInside.remove(zone.id)
                broadcastZoneEvent(ACTION_ZONE_EXITED, zone.name)
                updateNotification("⚠️ Left safe zone: ${zone.name}")
            }
        }
    }

    private fun checkHomeWifi() {
        val expectedSSID = prefs.getHomeWifiSSID() ?: return
        val network = connectivityManager.activeNetwork ?: return
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val nearHome = safeZones.any { it.isHome && DistanceUtils.distanceMeters(lastKnownLat, lastKnownLng, it.latitude, it.longitude) < 200 }
            if (nearHome) broadcastLocal(ACTION_HOME_ARRIVED)
        }
    }

    private fun startAnomalyMonitor() {
        if (anomalyCheckJob?.isActive == true) return
        anomalyCheckJob = serviceScope.launch {
            while (isActive) {
                delay(TimeUnit.MINUTES.toMillis(5))
                val now = System.currentTimeMillis()
                if (plannedRoutePoints.isNotEmpty() && now - lastMovementMs > ANOMALY_STATIONARY_MS) {
                    broadcastLocal(ACTION_ANOMALY_DETECTED) { putExtra(EXTRA_ANOMALY_MSG, "Stationary for too long") }
                }
                if (now - lastCheckinMs > ANOMALY_NO_CHECKIN_MS) {
                    broadcastLocal(ACTION_ANOMALY_DETECTED) { putExtra(EXTRA_ANOMALY_MSG, "No check-in for several hours") }
                }
            }
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val uid = auth.currentUser?.uid ?: return
                serviceScope.launch {
                    try {
                        locationRepository.flushOfflineQueue(uid)
                    } catch (e: Exception) {
                        Log.e(TAG, "Flush failed: ${e.message}")
                    }
                }
            }

            override fun onLost(network: Network) {
                updateNotification("📵 Offline — locations queued")
            }
        }
        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps    = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun listenForSafeZoneChanges() {
        val uid = auth.currentUser?.uid ?: return
        safeZoneListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                safeZones = snapshot.children.mapNotNull { it.getValue(SafeZone::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("safe_zones").child(uid).addValueEventListener(safeZoneListener!!)
    }

    private fun broadcastLocationUpdate(lat: Double, lng: Double, speed: Float) {
        broadcastLocal(ACTION_LOCATION_UPDATE) {
            putExtra(EXTRA_LATITUDE, lat); putExtra(EXTRA_LONGITUDE, lng); putExtra(EXTRA_SPEED, speed)
        }
    }

    private fun broadcastZoneEvent(action: String, zoneName: String) {
        broadcastLocal(action) { putExtra(EXTRA_ZONE_NAME, zoneName) }
    }

    private fun broadcastLocal(action: String, extras: (Intent.() -> Unit)? = null) {
        val intent = Intent(action)
        extras?.invoke(intent)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Aegis Location", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aegis Tracking").setContentText(text).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent).setOngoing(true).setPriority(NotificationCompat.PRIORITY_LOW).build()
    }

    private fun updateNotification(text: String) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }
}
