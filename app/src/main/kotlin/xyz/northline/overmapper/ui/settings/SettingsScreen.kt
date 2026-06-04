package xyz.northline.overmapper.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var weightInput by remember(prefs.weightKg) {
        mutableStateOf(prefs.weightKg?.let {
            if (prefs.weightUnit == "LBS") "%.1f".format(it * 2.20462f) else "%.1f".format(it)
        } ?: "")
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader("Profile")
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = {
                        val kg = weightInput.toFloatOrNull()?.let {
                            if (prefs.weightUnit == "LBS") it / 2.20462f else it
                        }
                        viewModel.setWeightKg(kg)
                    }) { Text("Save") }
                }
            )
            Row {
                listOf("KG", "LBS").forEach { unit ->
                    FilterChip(
                        selected = prefs.weightUnit == unit,
                        onClick = { viewModel.setWeightUnit(unit) },
                        label = { Text(unit) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        if (prefs.weightKg == null) {
            Text("Add your weight to get calorie estimates after each trail.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Recording")
        SettingsSlider("Min displacement", prefs.minDisplacementM, 3, 30, "m") {
            viewModel.setMinDisplacement(it)
        }
        SettingsSlider("Pause timeout", prefs.pauseTimeoutS, 15, 300, "s") {
            viewModel.setPauseTimeout(it)
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Display")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Gradient lines", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = prefs.gradientEnabled, onCheckedChange = viewModel::setGradientEnabled)
        }
        Text("Map tiles", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("OPENFREEMAP" to "Liberty", "OSM_STANDARD" to "Bright",
                "OSM_HUMANITARIAN" to "Positron").forEach { (key, label) ->
                FilterChip(
                    selected = prefs.mapTileSource == key,
                    onClick = { viewModel.setMapTileSource(key) },
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Data")
        OutlinedButton(
            onClick = { showClearDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear all trails and data") }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all data?") },
            text = { Text("All trails, photos and markers will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = { showClearDialog = false; viewModel.clearAllData {} },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun SettingsSlider(label: String, value: Int, min: Int, max: Int, unit: String,
                            onChanged: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("$value $unit", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value.toFloat(), onValueChange = { onChanged(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(), steps = max - min - 1)
    }
}
