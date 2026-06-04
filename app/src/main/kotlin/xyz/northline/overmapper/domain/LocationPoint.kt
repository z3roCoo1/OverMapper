package xyz.northline.overmapper.domain

import kotlin.math.*

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val timestampMs: Long
) {
    fun distanceTo(other: LocationPoint): Float {
        val r = 6_371_000.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
                sin(dLon / 2).pow(2)
        return (2 * r * asin(sqrt(a))).toFloat()
    }
}
