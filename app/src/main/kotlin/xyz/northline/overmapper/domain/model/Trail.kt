package xyz.northline.overmapper.domain.model

data class Trail(
    val id: Long,
    val recordedAt: Long,
    val distanceM: Float,
    val durationMs: Long,
    val elevationGainM: Float,
    val caloriesKcal: Float?,
    val bboxSwLat: Double, val bboxSwLon: Double,
    val bboxNeLat: Double, val bboxNeLon: Double,
    val note: String? = null
)
