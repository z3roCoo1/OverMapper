package xyz.northline.overmapper.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailDetailScreen(
    trailId: Long,
    onBack: () -> Unit,
    onViewOnMap: () -> Unit = {},
    viewModel: TrailDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trail by viewModel.trail.collectAsStateWithLifecycle()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val lat = trail?.let { t -> (t.bboxSwLat + t.bboxNeLat) / 2 } ?: 0.0
            val lon = trail?.let { t -> (t.bboxSwLon + t.bboxNeLon) / 2 } ?: 0.0
            viewModel.attachPhoto(context, it, trailId, lat, lon)
        }
    }
    val points by viewModel.points.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val markers by viewModel.markers.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        trail?.let {
                            SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it.recordedAt))
                        } ?: "Trail",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.requestViewOnMap()
                        onViewOnMap()
                    }) { Text("Map") }
                    TextButton(onClick = { viewModel.exportGpx(context) }) { Text("GPX") }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            trail?.let { t ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatBlock("Distance", "%.2f km".format(t.distanceM / 1000f))
                    StatBlock("Time", formatDuration(t.durationMs))
                    StatBlock("Elevation", "↑ %.0f m".format(t.elevationGainM))
                    t.caloriesKcal?.let { StatBlock("Calories", "~%.0f".format(it)) }
                }

                if (points.size >= 2) {
                    Text("Elevation", style = MaterialTheme.typography.titleLarge)
                    ElevationProfileChart(points)
                }
            }

            Text("Photos", style = MaterialTheme.typography.titleLarge)
            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos, key = { it.id }) { photo ->
                        AsyncImage(
                            model = photo.fileUri,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                Text("+ Add photo")
            }

            if (markers.isNotEmpty()) {
                Text("Conditions", style = MaterialTheme.typography.titleLarge)
                markers.forEach { marker ->
                    Text(
                        "• ${marker.type.name.lowercase().replaceFirstChar { it.uppercase() }}" +
                                (marker.body?.let { ": $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete trail?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTrail(onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatDuration(ms: Long): String {
    val h = ms / 3_600_000; val m = (ms % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
