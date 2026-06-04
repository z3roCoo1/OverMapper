package xyz.northline.overmapper.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.northline.overmapper.domain.model.RecordingState
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
    var showStopDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map, _ ->
                viewModel.onMapReady(map, prefs?.mapTileSource ?: "OPENFREEMAP")
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

        FloatingActionButton(
            onClick = {
                when (recordingState) {
                    is RecordingState.Idle -> viewModel.startRecording(context)
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
