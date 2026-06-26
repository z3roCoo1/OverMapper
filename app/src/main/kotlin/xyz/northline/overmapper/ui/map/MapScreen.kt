package xyz.northline.overmapper.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.northline.overmapper.domain.model.RecordingState
import xyz.northline.overmapper.ui.components.AddMarkerSheet
import xyz.northline.overmapper.ui.components.TrailBottomSheet

@Composable
fun MapScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val selectedTrailId by viewModel.selectedTrailId.collectAsStateWithLifecycle()
    val overlayVisible by viewModel.overlayVisible.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    var pendingMarkerLat by remember { mutableStateOf(0.0) }
    var pendingMarkerLon by remember { mutableStateOf(0.0) }
    var showAddMarker by remember { mutableStateOf(false) }
    var showLocationRationale by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.startRecording(context)
        } else {
            showLocationRationale = true
        }
    }

    fun launchRecording() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startRecording(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map, _ ->
                viewModel.onMapReady(map, prefs?.mapTileSource ?: "OPENFREEMAP")
                map.addOnMapLongClickListener { point ->
                    pendingMarkerLat = point.latitude
                    pendingMarkerLon = point.longitude
                    showAddMarker = true
                    true
                }
            }
        )

        if (recordingState is RecordingState.Recording) {
            RecordingPill(
                state = recordingState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = { viewModel.toggleOverlay() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(24.dp),
            containerColor = if (overlayVisible) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "Toggle trail overlay",
                tint = if (overlayVisible) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FloatingActionButton(
            onClick = {
                when (recordingState) {
                    is RecordingState.Idle -> launchRecording()
                    is RecordingState.Recording, is RecordingState.Paused ->
                        showStopDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (recordingState is RecordingState.Idle) {
                Text("⏺", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge)
            } else {
                Text("⏹", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge)
            }
        }

        selectedTrailId?.let { trailId ->
            TrailBottomSheet(
                trailId = trailId,
                onDismiss = { viewModel.selectTrail(null) },
                onOpenDetail = { onNavigateToDetail(trailId) }
            )
        }

        if (showAddMarker) {
            AddMarkerSheet(
                latitude = pendingMarkerLat,
                longitude = pendingMarkerLon,
                trailId = null,
                onSave = { type, body ->
                    viewModel.addMarker(pendingMarkerLat, pendingMarkerLon, null, type, body)
                    showAddMarker = false
                },
                onDismiss = { showAddMarker = false }
            )
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Location required") },
            text = { Text("Location permission is needed to record a route. Grant it in Settings.") },
            confirmButton = {
                TextButton(onClick = { showLocationRationale = false }) { Text("OK") }
            }
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop recording?") },
            text = { Text("Your trail will be saved.") },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    viewModel.stopRecording(context)
                }) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text("Continue") }
            }
        )
    }
}
