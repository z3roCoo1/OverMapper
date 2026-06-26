package xyz.northline.overmapper.domain.model

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float,
        val pointCount: Int,
        val lastLat: Double? = null,
        val lastLon: Double? = null
    ) : RecordingState()
    data class Paused(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float
    ) : RecordingState()
}
