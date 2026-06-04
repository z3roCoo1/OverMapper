package xyz.northline.overmapper.ui.trails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.model.Trail
import javax.inject.Inject

@HiltViewModel
class TrailsViewModel @Inject constructor(
    private val trailRepository: TrailRepository
) : ViewModel() {

    val trails = trailRepository.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun deleteTrail(trail: Trail) {
        viewModelScope.launch { trailRepository.deleteTrail(trail.id) }
    }
}
