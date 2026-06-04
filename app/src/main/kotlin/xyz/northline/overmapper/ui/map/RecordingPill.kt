package xyz.northline.overmapper.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.northline.overmapper.domain.model.RecordingState

@Composable
fun RecordingPill(state: RecordingState, modifier: Modifier = Modifier) {
    val recording = state as? RecordingState.Recording ?: return
    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(recording.startMs) {
        while (true) { delay(1_000L); tick++ }
    }

    val elapsed = System.currentTimeMillis() - recording.startMs
    val min = (elapsed / 60_000).toInt()
    val sec = ((elapsed % 60_000) / 1000).toInt()
    val km = recording.distanceM / 1000f

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⏺", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
        Text("%d:%02d".format(min, sec), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
        Text("%.2f km".format(km), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
    }
}
