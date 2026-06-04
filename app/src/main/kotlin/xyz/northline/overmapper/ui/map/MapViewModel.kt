package xyz.northline.overmapper.ui.map

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.expressions.Expression
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.db.entity.MarkerEntity
import xyz.northline.overmapper.data.repository.MarkerRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.TrailGeoJsonBuilder
import xyz.northline.overmapper.domain.model.MarkerType
import xyz.northline.overmapper.domain.model.RecordingState
import xyz.northline.overmapper.domain.model.TrailPoint
import xyz.northline.overmapper.service.RecordingService
import xyz.northline.overmapper.service.RecordingStateHolder
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val trailRepository: TrailRepository,
    private val markerRepository: MarkerRepository,
    private val stateHolder: RecordingStateHolder,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val recordingState: StateFlow<RecordingState> = stateHolder.state

    val preferences = prefsRepository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _selectedTrailId = MutableStateFlow<Long?>(null)
    val selectedTrailId: StateFlow<Long?> = _selectedTrailId.asStateFlow()

    private var mapLibreMap: MapLibreMap? = null
    private val OVERLAY_SOURCE = "trail-overlay"
    private val GRADIENT_SOURCE = "trail-gradient"
    private val OVERLAY_LAYER = "trail-overlay-layer"
    private val GRADIENT_LAYER = "trail-gradient-layer"

    init {
        viewModelScope.launch {
            combine(
                trailRepository.observeAll(),
                trailRepository.observeAllPoints(),
                prefsRepository.preferences
            ) { trails, points, prefs -> Triple(trails, points, prefs) }
                .collectLatest { (trails, points, prefs) ->
                    updateOverlay(trails.map { it.id }, points, prefs.gradientEnabled)
                }
        }
    }

    fun onMapReady(map: MapLibreMap, tileSource: String) {
        mapLibreMap = map
        map.setStyle(Style.Builder().fromUri(TileSource.styleUrl(tileSource))) { style ->
            style.addSource(GeoJsonSource(OVERLAY_SOURCE))
            style.addSource(GeoJsonSource(GRADIENT_SOURCE))
            style.addLayer(buildOverlayLayer())
            style.addLayer(buildGradientLayer())
        }
    }

    fun selectTrail(id: Long?) { _selectedTrailId.value = id }

    fun startRecording(context: Context) {
        context.startForegroundService(Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        })
    }

    fun stopRecording(context: Context) {
        context.startService(Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP
        })
    }

    private fun updateOverlay(
        trailIds: List<Long>,
        points: List<TrailPoint>,
        gradientEnabled: Boolean
    ) {
        val style = mapLibreMap?.style ?: return
        val overlayJson = TrailGeoJsonBuilder.buildOverlay(trailIds, points)
        style.getSourceAs<GeoJsonSource>(OVERLAY_SOURCE)?.setGeoJson(overlayJson)

        val gradientLayer = style.getLayer(GRADIENT_LAYER)
        if (gradientEnabled) {
            val gradientJson = TrailGeoJsonBuilder.buildGradient(points)
            style.getSourceAs<GeoJsonSource>(GRADIENT_SOURCE)?.setGeoJson(gradientJson)
            gradientLayer?.setProperties(visibility(Property.VISIBLE))
        } else {
            gradientLayer?.setProperties(visibility(Property.NONE))
        }
    }

    private fun buildOverlayLayer() = LineLayer(OVERLAY_LAYER, OVERLAY_SOURCE).apply {
        setProperties(
            lineColor(
                Expression.match(
                    Expression.get("age_bucket"),
                    Expression.literal("#B5562E"),
                    Expression.stop("new", "#B5562E"),
                    Expression.stop("mid", "#7E9A86"),
                    Expression.stop("old", "#8C7F6E")
                )
            ),
            lineWidth(3f),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND)
        )
    }

    private fun buildGradientLayer() = LineLayer(GRADIENT_LAYER, GRADIENT_SOURCE).apply {
        setProperties(
            lineColor(
                Expression.match(
                    Expression.get("slope"),
                    Expression.literal("#9CA3AF"),
                    Expression.stop("steep_up", "#D97706"),
                    Expression.stop("uphill", "#F59E0B"),
                    Expression.stop("flat", "#9CA3AF"),
                    Expression.stop("downhill", "#38BDF8"),
                    Expression.stop("steep_down", "#0284C7")
                )
            ),
            lineWidth(4f),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND)
        )
    }

    fun addMarker(lat: Double, lon: Double, trailId: Long?, type: MarkerType, body: String?) {
        viewModelScope.launch {
            markerRepository.insert(
                MarkerEntity(
                    trailId = trailId, latitude = lat, longitude = lon,
                    type = type.name, body = body,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
