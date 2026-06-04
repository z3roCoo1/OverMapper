package xyz.northline.overmapper.domain.model

data class TrailPoint(
    val id: Long,
    val trailId: Long,
    val segmentIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val recordedAt: Long
)
