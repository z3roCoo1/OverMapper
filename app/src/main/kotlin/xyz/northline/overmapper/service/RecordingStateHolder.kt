package xyz.northline.overmapper.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.northline.overmapper.domain.model.RecordingState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingStateHolder @Inject constructor() {
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    fun update(state: RecordingState) { _state.value = state }
}
