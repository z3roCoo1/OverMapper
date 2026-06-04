package xyz.northline.overmapper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.MarkerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerSheet(
    latitude: Double,
    longitude: Double,
    trailId: Long?,
    onSave: (MarkerType, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(MarkerType.NOTE) }
    var body by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add condition marker", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkerType.entries.forEach { type ->
                    FilterChip(
                        selected = type == selectedType,
                        onClick = { selectedType = type },
                        label = { Text(type.name.lowercase()
                            .replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { onSave(selectedType, body.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save marker") }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMarkerSheet(marker: Marker, onDelete: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(marker.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge)
            marker.body?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete marker") }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
