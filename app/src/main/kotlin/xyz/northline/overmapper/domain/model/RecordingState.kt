package xyz.northline.overmapper.domain.model

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float,
        val pointCount: Int
    ) : RecordingState()
    data class Paused(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float
    ) : RecordingState()
}
