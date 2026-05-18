package com.karthik.aegis.service

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
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
class AccidentDetectorService : Service(), SensorEventListener, LifecycleOwner {

    companion object {
        private const val TAG = "AccidentDetector"

        const val CHANNEL_ID      = "aegis_accident_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_CRASH_DETECTED     = "com.karthik.aegis.CRASH_DETECTED"
        const val ACTION_FALL_DETECTED      = "com.karthik.aegis.FALL_DETECTED"
        const val ACTION_FATIGUE_DETECTED   = "com.karthik.aegis.FATIGUE_DETECTED"
        const val ACTION_SHAKE_SOS          = "com.karthik.aegis.SHAKE_SOS"
        const val ACTION_AUTO_SOS_COUNTDOWN = "com.karthik.aegis.AUTO_SOS_COUNTDOWN"
        const val ACTION_AUTO_SOS_CANCELLED = "com.karthik.aegis.AUTO_SOS_CANCELLED"
        const val ACTION_CANCEL_COUNTDOWN   = "com.karthik.aegis.ACTION_CANCEL_COUNTDOWN"

        const val EXTRA_COUNTDOWN_SEC    = "countdown_sec"
        const val EXTRA_DETECTION_TYPE   = "detection_type"

        private const val CRASH_G_THRESHOLD       = 3.5f
        private const val CRASH_CONFIRM_WINDOW_MS  = 300L
        private const val CRASH_COOLDOWN_MS        = 10_000L
        private const val FREEFALL_G_THRESHOLD    = 0.4f
        private const val FREEFALL_MIN_MS         = 80L
        private const val IMPACT_G_THRESHOLD      = 2.5f
        private const val FALL_COOLDOWN_MS        = 8_000L
        private const val SHAKE_THRESHOLD         = 12f
        private const val SHAKE_COUNT_REQUIRED    = 5
        private const val SHAKE_WINDOW_MS         = 2_000L
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

        fun cancelCountdown(context: Context) {
            context.startService(
                Intent(context, AccidentDetectorService::class.java).apply {
                    action = ACTION_CANCEL_COUNTDOWN
                }
            )
        }
    }

    private val dispatcher = ServiceLifecycleDispatcher(this)

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    @Inject lateinit var prefs: AegisPrefs

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorHandlerThread: HandlerThread
    private var accelerometer: Sensor?      = null
    private var linearAcceleration: Sensor? = null
    private var gyroscope: Sensor?          = null

    private lateinit var faceDetector: FaceDetector
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var wakeLock: PowerManager.WakeLock

    private var crashEventStartMs   = 0L
    private var inCrashEvent        = false
    private var lastCrashMs         = 0L
    private var freefallStartMs     = 0L
    private var inFreefall          = false
    private var lastFallMs          = 0L
    private val shakeTimestamps     = ArrayDeque<Long>()
    private var lastShakeMs         = 0L
    private val blinkTimestamps     = ArrayDeque<Long>()
    private var eyeClosedStartMs    = 0L
    private var eyesCurrentlyClosed = false

    private var countdownJob: Job?  = null
    private var countdownActive     = false
    private val serviceScope        = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        
        notificationManager   = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        sensorManager         = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorHandlerThread   = HandlerThread("AegisSensorThread").also { it.start() }

        accelerometer      = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope          = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Aegis::DetectorWakeLock")

        setupFaceDetector()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        
        if (intent?.action == ACTION_CANCEL_COUNTDOWN) {
            cancelAutoSOS()
            return START_NOT_STICKY
        }

        val notification = buildNotification("Accident detection active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        registerSensors()

        if (prefs.isFatigueDetectionEnabled() &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraAnalysis()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
        sensorManager.unregisterListener(this)
        faceDetector.close()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        countdownJob?.cancel()
        serviceScope.cancel()
        sensorHandlerThread.quitSafely()
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return null
    }

    private fun registerSensors() {
        val handler = Handler(sensorHandlerThread.looper)
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, handler) }
        linearAcceleration?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, handler) }
        Log.d(TAG, "Sensors registered")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> processAccelerometer(event)
            Sensor.TYPE_LINEAR_ACCELERATION -> processLinearAcceleration(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun processAccelerometer(event: SensorEvent) {
        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        processCrashDetection(gForce)
        processFallDetection(gForce)
    }

    private fun processCrashDetection(gForce: Float) {
        val now = System.currentTimeMillis()
        if (now - lastCrashMs < CRASH_COOLDOWN_MS) return
        if (gForce >= CRASH_G_THRESHOLD && !inCrashEvent) {
            inCrashEvent = true
            crashEventStartMs = now
        } else if (gForce >= CRASH_G_THRESHOLD && inCrashEvent) {
            if (now - crashEventStartMs >= CRASH_CONFIRM_WINDOW_MS) {
                inCrashEvent = false
                lastCrashMs = now
                onCrashDetected(gForce)
            }
        } else { inCrashEvent = false }
    }

    private fun processFallDetection(gForce: Float) {
        val now = System.currentTimeMillis()
        if (now - lastFallMs < FALL_COOLDOWN_MS) return
        if (gForce < FREEFALL_G_THRESHOLD && !inFreefall) {
            inFreefall = true
            freefallStartMs = now
        } else if (inFreefall && gForce > IMPACT_G_THRESHOLD) {
            val duration = now - freefallStartMs
            if (duration >= FREEFALL_MIN_MS) {
                inFreefall = false
                lastFallMs = now
                onFallDetected(gForce, duration)
            } else { inFreefall = false }
        }
    }

    private fun processLinearAcceleration(event: SensorEvent) {
        val magnitude = sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2])
        if (magnitude > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeMs > 200) {
                shakeTimestamps.addLast(now)
                lastShakeMs = now
                while (shakeTimestamps.isNotEmpty() && now - shakeTimestamps.first() > SHAKE_WINDOW_MS) shakeTimestamps.removeFirst()
                if (shakeTimestamps.size >= SHAKE_COUNT_REQUIRED) {
                    shakeTimestamps.clear()
                    onShakeSOS()
                }
            }
        }
    }

    private fun setupFaceDetector() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        faceDetector = FaceDetection.getClient(FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build())
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
                cameraProvider?.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, analysis)
            } catch (e: Exception) { Log.e(TAG, "Camera error: ${e.message}") }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processCameraFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(image)
            .addOnSuccessListener { faces -> faces.firstOrNull()?.let { analyzeFace(it) } }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun analyzeFace(face: Face) {
        val leftOpen = face.leftEyeOpenProbability ?: return
        val rightOpen = face.rightEyeOpenProbability ?: return
        val now = System.currentTimeMillis()
        val closed = leftOpen < 0.2f && rightOpen < 0.2f
        if (closed) {
            if (!eyesCurrentlyClosed) { eyesCurrentlyClosed = true; eyeClosedStartMs = now }
            else if (now - eyeClosedStartMs >= EYE_CLOSED_THRESHOLD_MS) {
                eyesCurrentlyClosed = false
                onFatigueDetected("Drowsiness detected")
            }
        } else { eyesCurrentlyClosed = false }
    }

    private fun onCrashDetected(gForce: Float) {
        localBroadcastManager.sendBroadcast(Intent(ACTION_CRASH_DETECTED).putExtra(EXTRA_DETECTION_TYPE, "CRASH"))
        triggerAutoSOSCountdown("Vehicle Crash")
    }

    private fun onFallDetected(gForce: Float, duration: Long) {
        localBroadcastManager.sendBroadcast(Intent(ACTION_FALL_DETECTED).putExtra(EXTRA_DETECTION_TYPE, "FALL"))
        triggerAutoSOSCountdown("Fall Detected")
    }

    private fun onShakeSOS() {
        sendBroadcast(Intent(SOSBroadcastReceiver.ACTION_AUTO_SOS_FIRE).apply { putExtra("reason", "Shake SOS") })
    }

    private fun onFatigueDetected(reason: String) {
        localBroadcastManager.sendBroadcast(Intent(ACTION_FATIGUE_DETECTED).putExtra("reason", reason))
    }

    private fun triggerAutoSOSCountdown(reason: String) {
        if (countdownActive) return
        countdownActive = true
        countdownJob = serviceScope.launch {
            for (sec in AUTO_SOS_COUNTDOWN_SEC downTo 1) {
                localBroadcastManager.sendBroadcast(Intent(ACTION_AUTO_SOS_COUNTDOWN).putExtra(EXTRA_COUNTDOWN_SEC, sec))
                updateNotification("SOS in ${sec}s - Tap to cancel", true)
                delay(1000)
            }
            if (countdownActive) {
                sendBroadcast(Intent(SOSBroadcastReceiver.ACTION_AUTO_SOS_FIRE).apply { putExtra("reason", reason) })
                countdownActive = false
            }
        }
    }

    private fun cancelAutoSOS() {
        countdownActive = false
        countdownJob?.cancel()
        localBroadcastManager.sendBroadcast(Intent(ACTION_AUTO_SOS_CANCELLED))
        updateNotification("SOS Cancelled")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Aegis Safety", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String, showCancel: Boolean = false): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aegis Safety")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (showCancel) {
            val cancelIntent = Intent(this, AccidentDetectorService::class.java).apply { action = ACTION_CANCEL_COUNTDOWN }
            val pending = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, "Cancel", pending)
        }
        return builder.build()
    }

    private fun updateNotification(text: String, showCancel: Boolean = false) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text, showCancel))
    }
}
