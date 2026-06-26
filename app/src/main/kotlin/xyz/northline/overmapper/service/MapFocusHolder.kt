package xyz.northline.overmapper.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapFocusHolder @Inject constructor() {
    private val _focusTrailId = MutableStateFlow<Long?>(null)
    val focusTrailId: StateFlow<Long?> = _focusTrailId.asStateFlow()

    fun request(trailId: Long) { _focusTrailId.value = trailId }
    fun clear() { _focusTrailId.value = null }
}
