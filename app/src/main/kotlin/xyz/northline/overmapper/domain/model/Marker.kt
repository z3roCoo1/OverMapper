package xyz.northline.overmapper.domain.model

data class Marker(
    val id: Long,
    val trailId: Long?,
    val latitude: Double,
    val longitude: Double,
    val type: MarkerType,
    val body: String?,
    val createdAt: Long
)
