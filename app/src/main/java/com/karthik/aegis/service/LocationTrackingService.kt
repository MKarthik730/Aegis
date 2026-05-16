package com.karthik.aegis.service

// ─────────────────────────────────────────────────────────────────────────────
//   ✅ Hilt @AndroidEntryPoint injection (no manual repo instantiation)
//   ✅ Room DB offline queue — location syncs survive Firebase outages
//   ✅ NetworkCallback — flushes offline queue when connection returns
//   ✅ WakeLock — prevents CPU sleep during SOS + anomaly fire
//   ✅ directBootAware — works before user unlocks phone
//   ✅ Broadcasts use LocalBroadcastManager — not system-wide (secure)
//   ✅ WifiManager deprecation fix (API 29+)
//   ✅ Looper moved off main thread for location callbacks
//   ✅ Safe zone reload on Firebase change (ValueEventListener)
// ─────────────────────────────────────────────────────────────────────────────

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.*
import android.net.wifi.WifiManager
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

        // Broadcast actions — sent via LocalBroadcastManager (internal only)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
        }
    }

    // ── Hilt-injected dependencies ────────────────────────────────────────────

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var prefs: AegisPrefs

    // ── System services ───────────────────────────────────────────────────────

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var handlerThread: HandlerThread

    // ── Firebase ──────────────────────────────────────────────────────────────

    private val database = FirebaseDatabase.getInstance().reference
    private val auth     = FirebaseAuth.getInstance()

    // ── Coroutines ────────────────────────────────────────────────────────────

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── State ─────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        notificationManager  = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager  = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        // Dedicated background thread for location callbacks (off main thread)
        handlerThread = HandlerThread("AegisLocationThread").also { it.start() }

        // WakeLock — partial wake lock keeps CPU alive, screen can sleep
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Aegis::LocationWakeLock")

        createNotificationChannel()
        registerNetworkCallback()
        listenForSafeZoneChanges()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        trackingMode = intent?.getStringExtra("mode") ?: MODE_PASSIVE
        Log.d(TAG, "Starting in mode: $trackingMode")

        val notification = buildNotification("Aegis is protecting you 🛡️")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!wakeLock.isHeld) wakeLock.acquire(TimeUnit.HOURS.toMillis(12))

        startLocationUpdates()
        startAnomalyMonitor()
        return START_STICKY
    }

    override fun onDestroy() {
        super.destroy()
        Log.d(TAG, "Service destroyed")

        fusedLocationClient.removeLocationUpdates(locationCallback)
        anomalyCheckJob?.cancel()
        serviceScope.cancel()
        handlerThread.quitSafely()

        if (wakeLock.isHeld) wakeLock.release()

        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }

        safeZoneListener?.let {
            val uid = auth.currentUser?.uid ?: return
            database.child("safe_zones").child(uid).removeEventListener(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─────────────────────────────────────────────────────────────────────────
    // LOCATION UPDATES
    // ─────────────────────────────────────────────────────────────────────────

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted — stopping")
            stopSelf()
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onNewLocation(it) }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    Log.w(TAG, "Location unavailable — GPS signal lost")
                    updateNotification("⚠️ GPS signal lost — trying to reconnect...")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            buildLocationRequest(trackingMode),
            locationCallback,
            handlerThread.looper   // ✅ off main thread
        )
    }

    private fun buildLocationRequest(mode: String): LocationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            if (mode == MODE_ACTIVE) 5_000L else 30_000L
        ).apply {
            setMinUpdateIntervalMillis(if (mode == MODE_ACTIVE) 3_000L else 15_000L)
            setMinUpdateDistanceMeters(if (mode == MODE_ACTIVE) 5f else 20f)
            setWaitForAccurateLocation(false)
            setMaxUpdateDelayMillis(if (mode == MODE_ACTIVE) 10_000L else 60_000L)
        }.build()

    // ─────────────────────────────────────────────────────────────────────────
    // CORE LOCATION HANDLER
    // ─────────────────────────────────────────────────────────────────────────

    private fun onNewLocation(location: android.location.Location) {
        val lat   = location.latitude
        val lng   = location.longitude
        val speed = location.speed

        // Ignore implausible speeds (> 300 km/h = GPS glitch)
        if (speed > 83f) {
            Log.w(TAG, "Ignoring implausible speed: ${speed}m/s")
            return
        }

        broadcastLocationUpdate(lat, lng, speed)
        syncToFirebaseOrQueue(lat, lng, speed)
        checkSafeZones(lat, lng)
        if (plannedRoutePoints.isNotEmpty()) checkRouteDeviation(lat, lng)
        checkHomeWifi()
        autoSwitchMode(speed)

        val moved = DistanceUtils.distanceMeters(lastKnownLat, lastKnownLng, lat, lng)
        if (moved > 20) lastMovementMs = System.currentTimeMillis()

        lastKnownLat = lat
        lastKnownLng = lng
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIREBASE SYNC + ROOM OFFLINE QUEUE
    // ─────────────────────────────────────────────────────────────────────────

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
                    // Online — sync directly + flush any queued offline records
                    database.child("live_locations").child(uid).setValue(tracked).await()
                    locationRepository.flushOfflineQueue(uid)
                } else {
                    // Offline — save to Room DB queue
                    locationRepository.queueOfflineLocation(tracked)
                    Log.w(TAG, "Offline — location queued in Room DB")
                }
            } catch (e: Exception) {
                // Firebase failed — fallback to queue
                locationRepository.queueOfflineLocation(tracked)
                Log.e(TAG, "Firebase sync failed, queued: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NETWORK CALLBACK — flush queue when back online
    // ─────────────────────────────────────────────────────────────────────────

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available — flushing offline queue")
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
                Log.w(TAG, "Network lost — switching to offline queue")
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

    // ─────────────────────────────────────────────────────────────────────────
    // SAFE ZONE LISTENER — real-time Firebase updates
    // ─────────────────────────────────────────────────────────────────────────

    private fun listenForSafeZoneChanges() {
        val uid = auth.currentUser?.uid ?: return

        safeZoneListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                safeZones = snapshot.children.mapNotNull {
                    it.getValue(SafeZone::class.java)
                }
                Log.d(TAG, "Safe zones updated: ${safeZones.size} zones")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Safe zone listener cancelled: ${error.message}")
            }
        }

        database.child("safe_zones").child(uid)
            .addValueEventListener(safeZoneListener!!)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAFE ZONE / GEOFENCE CHECK
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkSafeZones(lat: Double, lng: Double) {
        safeZones.forEach { zone ->
            val distance = DistanceUtils.distanceMeters(lat, lng, zone.latitude, zone.longitude)
            val radius   = zone.radiusMeters.takeIf { it > 0 } ?: GEOFENCE_RADIUS_METERS
            val isInside = distance <= radius

            when {
                isInside && !zonesCurrentlyInside.contains(zone.id) -> {
                    zonesCurrentlyInside.add(zone.id)
                    broadcastZoneEvent(ACTION_ZONE_ENTERED, zone.name)
                    updateNotification("📍 Entered: ${zone.name}")
                    if (zone.isHome) broadcastHomeArrived()
                    Log.d(TAG, "Entered zone: ${zone.name} (${distance.toInt()}m from center)")
                }

                !isInside && zonesCurrentlyInside.contains(zone.id) -> {
                    zonesCurrentlyInside.remove(zone.id)
                    broadcastZoneEvent(ACTION_ZONE_EXITED, zone.name)
                    updateNotification("⚠️ Left safe zone: ${zone.name}")
                    if (zone.isHome) lastCheckinMs = System.currentTimeMillis()
                    Log.d(TAG, "Exited zone: ${zone.name}")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ROUTE DEVIATION
    // ─────────────────────────────────────────────────────────────────────────

    fun setPlannedRoute(routePoints: List<Pair<Double, Double>>) {
        plannedRoutePoints = routePoints
        Log.d(TAG, "Route set: ${routePoints.size} waypoints")
    }

    fun clearPlannedRoute() {
        plannedRoutePoints = emptyList()
    }

    private fun checkRouteDeviation(lat: Double, lng: Double) {
        val minDistance = plannedRoutePoints.minOfOrNull { (rLat, rLng) ->
            DistanceUtils.distanceMeters(lat, lng, rLat, rLng)
        } ?: return

        if (minDistance > ROUTE_DEVIATION_METERS) {
            broadcastLocal(ACTION_ROUTE_DEVIATION) {
                putExtra(EXTRA_LATITUDE, lat)
                putExtra(EXTRA_LONGITUDE, lng)
                putExtra(EXTRA_ANOMALY_MSG, "Deviated ${minDistance.toInt()}m from planned route")
            }
            updateNotification("⚠️ Off your planned route!")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HOME WIFI — API 29+ compatible
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkHomeWifi() {
        val expectedSSID = prefs.getHomeWifiSSID() ?: return

        // API 29+ deprecates WifiManager.connectionInfo — use NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork ?: return
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // Can't read SSID without location permission + Wi-Fi scan
                // Instead check if connected to WiFi at all when near home coords
                val nearHome = safeZones.any { zone ->
                    zone.isHome &&
                    DistanceUtils.distanceMeters(lastKnownLat, lastKnownLng, zone.latitude, zone.longitude) < 200
                }
                if (nearHome) broadcastHomeArrived()
            }
        } else {
            @Suppress("DEPRECATION")
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as? WifiManager
            val currentSSID = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            if (currentSSID == expectedSSID) broadcastHomeArrived()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANOMALY MONITOR
    // ─────────────────────────────────────────────────────────────────────────

    private fun startAnomalyMonitor() {
        anomalyCheckJob?.cancel()
        anomalyCheckJob = serviceScope.launch {
            while (isActive) {
                delay(TimeUnit.MINUTES.toMillis(5))
                val now = System.currentTimeMillis()

                if (plannedRoutePoints.isNotEmpty()) {
                    val stationaryMs = now - lastMovementMs
                    if (stationaryMs > ANOMALY_STATIONARY_MS) {
                        broadcastAnomaly(
                            "You haven't moved in ${stationaryMs / 60_000} minutes on your route."
                        )
        
