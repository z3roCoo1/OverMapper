package xyz.northline.overmapper.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val WEIGHT_KG = floatPreferencesKey("weight_kg")
    val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    val GRADIENT_ENABLED = booleanPreferencesKey("gradient_enabled")
    val MIN_DISPLACEMENT_M = intPreferencesKey("min_displacement_m")
    val PAUSE_TIMEOUT_S = intPreferencesKey("pause_timeout_s")
    val MAP_TILE_SOURCE = stringPreferencesKey("map_tile_source")
}
