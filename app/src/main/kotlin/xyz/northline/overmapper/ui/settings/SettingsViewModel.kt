package xyz.northline.overmapper.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.datastore.UserPreferences
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
    private val trailRepository: TrailRepository
) : ViewModel() {

    val prefs = prefsRepo.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        UserPreferences()
    )

    fun setWeightKg(kg: Float?) = viewModelScope.launch { prefsRepo.setWeightKg(kg) }
    fun setWeightUnit(unit: String) = viewModelScope.launch { prefsRepo.setWeightUnit(unit) }
    fun setGradientEnabled(v: Boolean) = viewModelScope.launch { prefsRepo.setGradientEnabled(v) }
    fun setMinDisplacement(m: Int) = viewModelScope.launch { prefsRepo.setMinDisplacementM(m) }
    fun setPauseTimeout(s: Int) = viewModelScope.launch { prefsRepo.setPauseTimeoutS(s) }
    fun setMapTileSource(s: String) = viewModelScope.launch { prefsRepo.setMapTileSource(s) }

    fun clearAllData(onDone: () -> Unit) = viewModelScope.launch {
        trailRepository.observeAll().first().forEach {
            trailRepository.deleteTrail(it.id)
        }
        onDone()
    }
}
