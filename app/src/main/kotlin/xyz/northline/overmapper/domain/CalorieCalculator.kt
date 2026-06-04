package xyz.northline.overmapper.domain

object CalorieCalculator {
    fun calculate(
        weightKg: Float,
        durationMs: Long,
        distanceM: Float,
        elevationGainM: Float
    ): Float {
        if (durationMs <= 0L) return 0f
        val durationHours = durationMs / 3_600_000f
        val distanceKm = distanceM / 1000f
        val paceMinPerKm = if (distanceKm > 0) (durationMs / 60_000f) / distanceKm else 20f
        val paceDelta = ((20f - paceMinPerKm.coerceIn(8f, 30f)) / 12f) * 1.5f
        val elevDelta = if (distanceKm > 0) (elevationGainM / distanceKm / 100f) * 0.8f else 0f
        val met = (5.5f + paceDelta + elevDelta).coerceIn(4f, 12f)
        return met * weightKg * durationHours
    }
}
