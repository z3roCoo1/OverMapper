package xyz.northline.overmapper.domain.model

data class TrailPhoto(
    val id: Long,
    val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    val fileUri: String,
    val takenAt: Long
)
