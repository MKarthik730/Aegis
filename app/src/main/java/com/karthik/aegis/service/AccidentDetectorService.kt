package com.karthik.aegis.service

// ─────────────────────────────────────────────────────────────────────────────
//   ✅ @AndroidEntryPoint — Hilt DI replaces manual instantiation
//   ✅ LifecycleService properly used — CameraX binds to service lifecycle
//   ✅ WakeLock acquired during auto-SOS countdown (CPU stays alive)
//   ✅ cancelAutoSOS() triggered via Intent (not method call from outside)
//   ✅ Sensor registration on dedicated HandlerThread (off main looper)
//   ✅ Camera only started when CAMERA permission granted
//   ✅ Fatigue detection gated behind prefs check at runtime
//   ✅ Countdown notification has PendingIntent cancel action button
//   ✅ LocalBroadcastManager for internal broadcasts
// ─────────────────────────────────────────────────────────────────────────────

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.karthik.aegis.R
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class AccidentDetectorService : LifecycleService(), SensorEventListener {

    companion object {
        private const val TAG = "AccidentDetector"

        const val CHANNEL_ID      = "aegis_accident_channel"
        const val NOTIFICATION_ID = 1002

        // Intent actions
        const val ACTION_CRASH_DETECTED     = "com.karthik.aegis.CRASH_DETECTED"
        const val ACTION_FALL_DETECTED      = "com.karthik.aegis.FALL_DETECTED"
        const val ACTION_FATIGUE_DETECTED   = "com.karthik.aegis.FATIGUE_DETECTED"
        const val ACTION_SHAKE_SOS          = "com.karthik.aegis.SHAKE_SOS"
        const val ACTION_AUTO_SOS_COUNTDOWN = "com.karthik.aegis.AUTO_SOS_COUNTDOWN"
        const val ACTION_AUTO_SOS_CANCELLED = "com.karthik.aegis.AUTO_SOS_CANCELLED"

        // Handled via onStartCommand
        const val ACTION_CANCEL_COUNTDOWN   = "com.karthik.aegis.ACTION_CANCEL_COUNTDOWN"

        const val EXTRA_COUNTDOWN_SEC    = "countdown_sec"
        const val EXTRA_DETECTION_TYPE   = "detection_type"

        // Crash
        private const val CRASH_G_THRESHOLD       = 3.5f
        private const val CRASH_CONFIRM_WINDOW_MS  = 300L
        private const val CRASH_COOLDOWN_MS        = 10_000L

        // Fall
        private const val FREEFALL_G_THRESHOLD    = 0.4f
        private const val FREEFALL_MIN_MS         = 80L
        private const val IMPACT_G_THRESHOLD      = 2.5f
        private const val FALL_COOLDOWN_MS        = 8_000L

        // Shake SOS
        private const val SHAKE_THRESHOLD         = 12f
        private const val SHAKE_COUNT_REQUIRED    = 5
        private const val SHAKE_WINDOW_MS         = 2_000L

        // Fatigue
        private const val BLINK_RATE_DROWSY       = 8
        private const val EYE_CLOSED_THRESHOLD_MS = 1_500L
        private const val FATIGUE_WINDOW_MS       = 60_000L

        const val AUTO_SOS_COUNTDOWN_SEC          = 30

        fun start(context: Context) {
            val intent = Intent(context, AccidentDetectorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AccidentDetectorService::class.java))
        }

        /** Cancel countdown from outside — sends Intent to service */
        fun cancelCountdown(context: Context) {
            context.startService(
                Intent(context, AccidentDetectorService::class.java).apply {
                    action = ACTION_CANCEL_COUNTDOWN
                }
            )
        }
    }

    // ── Hilt ─────────────────────────────────────────────────────────────────

    @Inject lateinit var prefs: AegisPrefs

    // ── Hardware ──────────────────────────────────────────────────────────────

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorHandlerThread: HandlerThread
    private var accelerometer: Sensor?      = null
    private var gyroscope: Sensor?          = null
    private var linearAcceleration: Sensor? = null

    // ── ML Kit ────────────────────────────────────────────────────────────────

    private lateinit var faceDetector: FaceDetector
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    // ── System ────────────────────────────────────────────────────────────────

    private lateinit var notificationManager: NotificationManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var wakeLock: PowerManager.WakeLock

    // ── State — Crash ─────────────────────────────────────────────────────────

    private var crashEventStartMs   = 0L
    private var inCrashEvent        = false
    private var lastCrashMs         = 0L

    // ── State — Fall ──────────────────────────────────────────────────────────

    private var freefallStartMs     = 0L
    private var inFreefall          = false
    private var lastFallMs          = 0L

    // ── State — Shake ─────────────────────────────────────────────────────────

    private val shakeTimestamps     = ArrayDeque<Long>()
    private var lastShakeMs         = 0L

    // ── State — Fatigue ───────────────────────────────────────────────────────

    private val blinkTimestamps     = ArrayDeque<Long>()
    private var eyeClosedStartMs    = 0L
    private var eyesCurrentlyClosed = false

    // ── Countdown ─────────────────────────────────────────────────────────────

    private var countdownJob: Job?  = null
    private var countdownActive     = false
    private val serviceScope        = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        notificationManager   = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        sensorManager         = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorHandlerThread   = HandlerThread("AegisSensorThread").also { it.start() }

        accelerometer      = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope          = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Aegis::DetectorWakeLock")

        setupFaceDetector()
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Handle cancel countdown Intent
        if (intent?.action == ACTION_CANCEL_COUNTDOWN) {
            cancelAutoSOS()
            return START_NOT_STICKY
        }

        val notification = buildNotification("Accident detection active")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        registerSensors()

        // Only start camera if permission granted AND feature enabled
        if (prefs.isFatigueDetectionEnabled() &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraAnalysis()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        faceDetector.close()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        countdownJob?.cancel()
        serviceScope.cancel()
        sensorHandlerThread.quitSafely()
        if (wakeLock.isHeld) wakeLock.release()
        Log.d(TAG, "Service destroyed")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SENSORS
    // ─────────────────────────────────────────────────────────────────────────

    private fun registerSensors() {
        val handler = Handler(sensorHandlerThread.looper)

        accelerometer?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_FASTEST,
                handler
            )
        }
        linearAcceleration?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_FASTEST,
                handler
            )
        }
        gyroscope?.let {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_GAME,
                handler
            )
        }
        Log.d(TAG, "Sensors registered on dedicated thread")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER        -> processAccelerometer(event)
            Sensor.TYPE_LINEAR_ACCELERATION  -> processLinearAcceleration(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* not needed */ }

    // ─────────────────────────────────────────────────────────────────────────
    // CRASH DETECTION
    // ─────────────────────────────────────────────────────────────────────────

    private fun processAccelerometer(event: SensorEvent) {
        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH

        processCrashDetection(gForce)
        processFallDetection(gForce)
    }

    private fun processCrashDetection(gForce: Float) {
        val now = System.currentTimeMillis()
        if (now - lastCrashMs < CRASH_COOLDOWN_MS) return

        when {
            gForce >= CRASH_G_THRESHOLD && !inCrashEvent -> {
                inCrashEvent      = true
                crashEventStartMs = now
            }
            gForce >= CRASH_G_THRESHOLD && inCrashEvent -> {
                if (now - crashEventStartMs >= CRASH_CONFIRM_WINDOW_MS) {
                    inCrashEvent = false
                    lastCrashMs  = now
                    onCrashDetected(gForce)
                }
            }
            else -> { if (inCrashEvent) inCrashEvent = false }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALL DETECTION
    // ─────────────────────────────────────────────────────────────────────────

    private fun processFallDetection(gForce: Float) {
        val now = System.currentTimeMillis()
        if (now - lastFallMs < FALL_COOLDOWN_MS) return

        when {
            gForce < FREEFALL_G_THRESHOLD && !inFreefall -> {
                inFreefall      = true
                freefallStartMs = now
            }
            inFreefall && gForce > IMPACT_G_THRESHOLD -> {
                val duration = now - freefallStartMs
                if (duration >= FREEFALL_MIN_MS) {
                    inFreefall = false
                    lastFallMs = now
                    onFallDetected(gForce, duration)
                } else {
                    inFreefall = false
                }
            }
            inFreefall && (now - freefallStartMs) > 3_000L -> {
                inFreefall = false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHAKE SOS
    // ─────────────────────────────────────────────────────────────────────────

    private fun processLinearAcceleration(event: SensorEvent) {
        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        if (magnitude > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeMs > 200) {
                shakeTimestamps.addLast(now)
                lastShakeMs = now
                while (shakeTimestamps.isNotEmpty() &&
                    now - shakeTimestamps.first() > SHAKE_WINDOW_MS) {
                    shakeTimestamps.removeFirst()
                }
                if (shakeTimestamps.size >= SHAKE_COUNT_REQUIRED) {
                    shakeTimestamps.clear()
                    onShakeSOS()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FATIGUE — ML Kit (CameraX bound to LifecycleService)
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupFaceDetector() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        faceDetector   = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.3f)
                .build()
        )
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startCameraAnalysis() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                cameraProvider = future.get()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, ::processCameraFrame) }

                cameraProvider?.unbindAll()
                // ✅ binds to THIS LifecycleService — no external LifecycleOwner needed
                cameraProvider?.bindToLifecycle(
                    this,                            // LifecycleService IS a LifecycleOwner
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    analysis
                )
                Log.d(TAG, "Camera analysis started")
            } catch (e: Exception) {
                Log.e(TAG, "Camera setup failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processCameraFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        faceDetector.process(image)
            .addOnSuccessListener { faces -> faces.firstOrNull()?.let { analyzeFace(it) } }
            .addOnFailureListener { Log.w(TAG, "Face detect error: ${it.message}") }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun analyzeFace(face: Face) {
        val leftOpen  = face.leftEyeOpenProbability  ?: return
        val rightOpen = face.rightEyeOpenProbability ?: return
        val now       = System.currentTimeMillis()
        val closed    = leftOpen < 0.2f && rightOpen < 0.2f

        if (closed) {
            if (!eyesCurrentlyClosed) {
                eyesCurrentlyClosed = true
                eyeClosedStartMs    = now
            } else if (now - eyeClosedStartMs >= EYE_CLOSED_THRESHOLD_MS) {
                eyesCurrentlyClosed = false
                onFatigueDetected("Microsleep detected — eyes closed ${(now - eyeClosedStartMs) / 1000}s")
            }
        } else {
            if (eyesCurrentlyClosed) {
                eyesCurrentlyClosed = false
                blinkTimestamps.addLast(now)
                while (blinkTimestamps.isNotEmpty() &&
                    now - blinkTimestamps.first() > FATIGUE_WINDOW_MS) {
                    blinkTimestamps.removeFirst()
                }
                if (blinkTimestamps.size >= 10 && blinkTimestamps.size < BLINK_RATE_DROWSY) {
                    onFatigueDetected("Low blink rate (${blinkTimestamps.size}/min) — possible drowsiness")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DETECTION HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun onCrashDetected(gForce: Float) {
        localBroadcastManager.sendBroadcast(
            Intent(ACTION_CRASH_DETECTED).putExtra(EXTRA_DETECTION_TYPE, "CRASH")
        )
        triggerAutoSOSCountdown("Vehicle crash (${String.format("%.1f", gForce)}G)")
    }

    private fun onFallDetected(gForce: Float, freefallMs: Long) {
        localBroadcastManager.sendBroadcast(
            Intent(ACTION_FALL_DETECTED).putExtra(EXTRA_DETECTION_TYPE, "FALL")
        )
        triggerAutoSOSCountdown("Fall detected (${freefallMs}ms freefall)")
    }

    private fun onShakeSOS() {
        // Intentional — fire immediately through global broadcast to SOSBroadcastReceiver
        sendBroadcast(Intent(SOSBroadcastReceiver.ACTION_AUTO_SOS_FIRE).apply {
            putExtra("reason", "Shake SOS")
        })
        updateNotification("🆘 Shake SOS triggered!")
    }

    private fun onFatigueDetected(reason: String) {
        localBroadcastManager.sendBroadcast(
            Intent(ACTION_FATIGUE_DETECTED)
                .putExtra(EXTRA_DETECTION_TYPE, "FATIGUE")
                .putExtra("reason", reason)
        )
        updateNotification("⚠️ Fatigue warning!")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTO-SOS COUNTDOWN
    // ─────────────────────────────────────────────────────────────────────────

    private fun triggerAutoSOSCountdown(reason: String) {
        if (countdownActive) return

        countdownActive = true
        if (!wakeLock.isHeld) wakeLock.acquire(TimeUnit.SECONDS.toMillis(
            AUTO_SOS_COUNTDOWN_SEC + 5L
        ))
        Log.w(TAG, "Auto-SOS countdown: $reason")

        countdownJob = serviceScope.launch {
            for (sec in AUTO_SOS_COUNTDOWN_SEC downTo 1) {
                localBroadcastManager.sendBroadcast(
                    Intent(ACTION_AUTO_SOS_COUNTDOWN)
                        .putExtra(EXTRA_COUNTDOWN_SEC, sec)
                        .putExtra("reason", reason)
                )
                updateNotification("🆘 SOS in ${sec}s — tap to cancel", showCancelAction = true)
                delay(1_000L)
            }

            if (countdownActive) {
                Log.e(TAG, "Auto-SOS FIRED: $reason")
                sendBroadcast(Intent(SOSBroadcastReceiver.ACTION_AUTO_SOS_FIRE).apply {
                    putExtra("reason", reason)
                })
                countdownActive = false
                if (wakeLock.isHeld) wakeLock.release()
            }
     
