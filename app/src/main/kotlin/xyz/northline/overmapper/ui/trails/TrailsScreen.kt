package xyz.northline.overmapper.ui.trails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import xyz.northline.overmapper.domain.model.Trail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailsScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TrailsViewModel = hiltViewModel()
) {
    val trails by viewModel.trails.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Trails", style = MaterialTheme.typography.headlineMedium) })
        }
    ) { padding ->
        if (trails.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No trails yet.\nStart recording from the map.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trails, key = { it.id }) { trail ->
                    SwipeToDeleteTrailCard(
                        trail = trail,
                        onTap = { onNavigateToDetail(trail.id) },
                        onDelete = {
                            viewModel.deleteTrail(trail)
                            scope.launch {
                                snackbarHostState.showSnackbar("Trail deleted", "Undo",
                                    duration = SnackbarDuration.Short)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTrailCard(trail: Trail, onTap: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
    )

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        TrailCard(trail = trail, onTap = onTap)
    }
}

@Composable
private fun TrailCard(trail: Trail, onTap: () -> Unit) {
    val dateStr = remember(trail.recordedAt) {
        SimpleDateFormat("EEE d MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(trail.recordedAt))
    }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(dateStr, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("%.2f km".format(trail.distanceM / 1000f))
                StatChip(formatDuration(trail.durationMs))
                StatChip("↑ %.0f m".format(trail.elevationGainM))
                trail.caloriesKcal?.let { StatChip("~%.0f kcal".format(it)) }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun formatDuration(ms: Long): String {
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
