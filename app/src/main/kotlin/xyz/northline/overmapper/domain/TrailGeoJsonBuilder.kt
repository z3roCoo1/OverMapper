package xyz.northline.overmapper.domain

import xyz.northline.overmapper.domain.model.TrailPoint

object TrailGeoJsonBuilder {

    fun buildOverlay(trailIds: List<Long>, points: List<TrailPoint>): String {
        if (points.isEmpty()) return """{"type":"FeatureCollection","features":[]}"""

        val byTrail = points.groupBy { it.trailId }
        val features = mutableListOf<String>()

        trailIds.forEachIndexed { index, trailId ->
            val ageBucket = when {
                index == trailIds.lastIndex -> "new"
                index >= trailIds.size - 3 -> "mid"
                else -> "old"
            }
            val trailPoints = byTrail[trailId] ?: return@forEachIndexed
            val bySegment = trailPoints.groupBy { it.segmentIndex }.toSortedMap()
            bySegment.values.forEach { segPts ->
                if (segPts.size < 2) return@forEach
                val coords = segPts.joinToString(",") { "[${it.longitude},${it.latitude}]" }
                features.add("""{"type":"Feature","properties":{"trail_id":$trailId,"age_bucket":"$ageBucket"},"geometry":{"type":"LineString","coordinates":[$coords]}}""")
            }
        }

        return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
    }

    fun buildGradient(points: List<TrailPoint>): String {
        val features = mutableListOf<String>()
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            if (a.segmentIndex != b.segmentIndex) continue
            val distM = LocationPoint(a.latitude, a.longitude, a.altitudeM, a.recordedAt)
                .distanceTo(LocationPoint(b.latitude, b.longitude, b.altitudeM, b.recordedAt))
            val slopePct = if (distM > 0) (b.altitudeM - a.altitudeM) / distM * 100.0 else 0.0
            val slope = when {
                slopePct > 10 -> "steep_up"
                slopePct > 3 -> "uphill"
                slopePct < -10 -> "steep_down"
                slopePct < -3 -> "downhill"
                else -> "flat"
            }
            features.add("""{"type":"Feature","properties":{"slope":"$slope"},"geometry":{"type":"LineString","coordinates":[[${a.longitude},${a.latitude}],[${b.longitude},${b.latitude}]]}}""")
        }
        return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
    }
}
