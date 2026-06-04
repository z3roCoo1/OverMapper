package xyz.northline.overmapper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.northline.overmapper.MainActivity
import xyz.northline.overmapper.R
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.db.entity.TrailEntity
import xyz.northline.overmapper.data.db.entity.TrailPointEntity
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.CalorieCalculator
import xyz.northline.overmapper.domain.FilterConfig
import xyz.northline.overmapper.domain.FilterResult
import xyz.northline.overmapper.domain.LocationPoint
import xyz.northline.overmapper.domain.StationaryFilter
import xyz.northline.overmapper.domain.model.RecordingState
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : LifecycleService() {

    @Inject lateinit var stateHolder: RecordingStateHolder
    @Inject lateinit var trailRepository: TrailRepository
    @Inject lateinit var prefsRepository: UserPreferencesRepository

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var filter: StationaryFilter = StationaryFilter(FilterConfig())
    private var notificationJob: Job? = null

    private var currentTrailId: Long = -1L
    private var startMs: Long = 0L
    private var distanceM: Float = 0f
    private var segmentIndex: Int = 0
    private var lastAcceptedPoint: LocationPoint? = null
    private var pendingPoints = mutableListOf<TrailPointEntity>()

    private val CHANNEL_ID = "recording"
    private val NOTIF_ID = 1

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch { startRecording() }
            ACTION_STOP -> lifecycleScope.launch { stopRecording() }
        }
        return START_STICKY
    }

    private suspend fun startRecording() {
        val prefs = prefsRepository.preferences.first()
        filter = StationaryFilter(FilterConfig(
            minDisplacementM = prefs.minDisplacementM.toFloat(),
            pauseTimeoutMs = prefs.pauseTimeoutS * 1000L
        ))

        val trailEntity = TrailEntity(
            recordedAt = System.currentTimeMillis(),
            distanceM = 0f, durationMs = 0L, elevationGainM = 0f,
            caloriesKcal = null,
            bboxSwLat = 0.0, bboxSwLon = 0.0, bboxNeLat = 0.0, bboxNeLon = 0.0
        )
        currentTrailId = trailRepository.insertTrail(trailEntity)
        startMs = System.currentTimeMillis()
        distanceM = 0f
        segmentIndex = 0
        lastAcceptedPoint = null
        pendingPoints.clear()

        stateHolder.update(RecordingState.Recording(currentTrailId, startMs, 0f, 0))
        startForeground(NOTIF_ID, buildNotification("Recording trail", "0:00 • 0.00 km"))
        startLocationUpdates(prefs.minDisplacementM)
        startNotificationUpdater()
    }

    private suspend fun stopRecording() {
        stopLocationUpdates()
        notificationJob?.cancel()
        flushPoints()

        val durationMs = System.currentTimeMillis() - startMs
        val prefs = prefsRepository.preferences.first()
        val calories = prefs.weightKg?.let {
            CalorieCalculator.calculate(it, durationMs, distanceM, 0f)
        }

        val existing = trailRepository.getById(currentTrailId)
        if (existing != null) {
            trailRepository.updateTrail(
                TrailEntity(
                    id = currentTrailId,
                    recordedAt = existing.recordedAt,
                    distanceM = distanceM,
                    durationMs = durationMs,
                    elevationGainM = 0f,
                    caloriesKcal = calories,
                    bboxSwLat = existing.bboxSwLat, bboxSwLon = existing.bboxSwLon,
                    bboxNeLat = existing.bboxNeLat, bboxNeLon = existing.bboxNeLon
                )
            )
        }

        stateHolder.update(RecordingState.Idle)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates(minDisplacementM: Int) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(minDisplacementM.toFloat())
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val incoming = LocationPoint(loc.latitude, loc.longitude, loc.altitude, loc.time)
                    lifecycleScope.launch { handleLocation(incoming) }
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) { stopSelf() }
    }

    private suspend fun handleLocation(point: LocationPoint) {
        val now = System.currentTimeMillis()
        when (val result = filter.process(point, now)) {
            is FilterResult.Accept -> {
                addPoint(point, segmentIndex)
                lastAcceptedPoint?.let { distanceM += it.distanceTo(point) }
                lastAcceptedPoint = point
                stateHolder.update(RecordingState.Recording(currentTrailId, startMs, distanceM, pendingPoints.size))
            }
            is FilterResult.Resume -> {
                segmentIndex++
                addPoint(point, segmentIndex)
                lastAcceptedPoint = point
                stateHolder.update(RecordingState.Recording(currentTrailId, startMs, distanceM, pendingPoints.size))
            }
            FilterResult.Pause -> {
                stateHolder.update(RecordingState.Paused(currentTrailId, startMs, distanceM))
                updateNotification("Paused — not moving", formatStats())
            }
            FilterResult.Discard -> Unit
        }

        if (pendingPoints.size >= 20) flushPoints()
    }

    private fun addPoint(point: LocationPoint, segment: Int) {
        pendingPoints.add(TrailPointEntity(
            trailId = currentTrailId,
            segmentIndex = segment,
            latitude = point.latitude,
            longitude = point.longitude,
            altitudeM = point.altitudeM,
            recordedAt = point.timestampMs
        ))
    }

    private suspend fun flushPoints() {
        if (pendingPoints.isNotEmpty()) {
            trailRepository.insertPoints(pendingPoints.toList())
            pendingPoints.clear()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun startNotificationUpdater() {
        notificationJob = lifecycleScope.launch {
            while (true) {
                delay(1_000L)
                if (stateHolder.state.value is RecordingState.Recording) {
                    updateNotification("Recording trail", formatStats())
                }
            }
        }
    }

    private fun formatStats(): String {
        val elapsed = System.currentTimeMillis() - startMs
        val minutes = (elapsed / 60_000).toInt()
        val seconds = ((elapsed % 60_000) / 1000).toInt()
        val km = distanceM / 1000f
        return "%d:%02d • %.2f km".format(minutes, seconds, km)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

    private fun updateNotification(title: String, text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(title, text))
    }
}
