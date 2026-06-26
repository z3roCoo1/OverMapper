package xyz.northline.overmapper.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import xyz.northline.overmapper.data.db.entity.TrailPhotoEntity
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.repository.MarkerRepository
import xyz.northline.overmapper.data.repository.PhotoRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.GpxExporter
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPhoto
import xyz.northline.overmapper.domain.model.TrailPoint
import xyz.northline.overmapper.service.MapFocusHolder
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrailDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trailRepository: TrailRepository,
    private val markerRepository: MarkerRepository,
    private val photoRepository: PhotoRepository,
    private val mapFocusHolder: MapFocusHolder
) : ViewModel() {

    private val trailId: Long = checkNotNull(savedStateHandle["trailId"])

    val trail: StateFlow<Trail?> = flow { emit(trailRepository.getById(trailId)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val points: StateFlow<List<TrailPoint>> = flow {
        emit(trailRepository.getPointsForTrail(trailId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val photos: StateFlow<List<TrailPhoto>> = photoRepository.observeByTrailId(trailId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val markers: StateFlow<List<Marker>> = markerRepository.observeAll()
        .map { all -> all.filter { it.trailId == trailId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exportGpx(context: Context) {
        viewModelScope.launch {
            val t = trail.value ?: return@launch
            val pts = points.value
            val gpx = GpxExporter.export(t, pts)
            val file = File(context.cacheDir, "overmapper_trail_${trailId}.gpx")
            file.writeText(gpx)
            val uri = FileProvider.getUriForFile(context,
                "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export GPX"))
        }
    }

    fun attachPhoto(context: Context, uri: Uri, trailId: Long, lat: Double, lon: Double) {
        viewModelScope.launch {
            photoRepository.insert(
                TrailPhotoEntity(
                    trailId = trailId,
                    latitude = lat,
                    longitude = lon,
                    fileUri = uri.toString(),
                    takenAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deletePhoto(photo: TrailPhoto) {
        viewModelScope.launch { photoRepository.delete(photo) }
    }

    fun requestViewOnMap() {
        mapFocusHolder.request(trailId)
    }

    fun deleteTrail(onDeleted: () -> Unit) {
        viewModelScope.launch {
            trailRepository.deleteTrail(trailId)
            onDeleted()
        }
    }
}
