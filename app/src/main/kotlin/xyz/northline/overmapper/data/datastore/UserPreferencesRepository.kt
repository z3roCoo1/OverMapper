package xyz.northline.overmapper.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val weightKg: Float? = null,
    val weightUnit: String = "KG",
    val gradientEnabled: Boolean = true,
    val minDisplacementM: Int = 8,
    val pauseTimeoutS: Int = 60,
    val mapTileSource: String = "OPENFREEMAP"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            weightKg = prefs[PreferencesKeys.WEIGHT_KG],
            weightUnit = prefs[PreferencesKeys.WEIGHT_UNIT] ?: "KG",
            gradientEnabled = prefs[PreferencesKeys.GRADIENT_ENABLED] ?: true,
            minDisplacementM = prefs[PreferencesKeys.MIN_DISPLACEMENT_M] ?: 8,
            pauseTimeoutS = prefs[PreferencesKeys.PAUSE_TIMEOUT_S] ?: 60,
            mapTileSource = prefs[PreferencesKeys.MAP_TILE_SOURCE] ?: "OPENFREEMAP"
        )
    }

    suspend fun setWeightKg(value: Float?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(PreferencesKeys.WEIGHT_KG)
            else prefs[PreferencesKeys.WEIGHT_KG] = value
        }
    }

    suspend fun setWeightUnit(unit: String) {
        dataStore.edit { it[PreferencesKeys.WEIGHT_UNIT] = unit }
    }

    suspend fun setGradientEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.GRADIENT_ENABLED] = enabled }
    }

    suspend fun setMinDisplacementM(value: Int) {
        dataStore.edit { it[PreferencesKeys.MIN_DISPLACEMENT_M] = value }
    }

    suspend fun setPauseTimeoutS(value: Int) {
        dataStore.edit { it[PreferencesKeys.PAUSE_TIMEOUT_S] = value }
    }

    suspend fun setMapTileSource(source: String) {
        dataStore.edit { it[PreferencesKeys.MAP_TILE_SOURCE] = source }
    }
}
