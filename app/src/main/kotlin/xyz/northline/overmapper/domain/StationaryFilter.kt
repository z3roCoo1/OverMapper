package xyz.northline.overmapper.domain

data class FilterConfig(
    val minDisplacementM: Float = 8f,
    val pauseTimeoutMs: Long = 60_000L
)

sealed class FilterResult {
    data class Accept(val point: LocationPoint) : FilterResult()
    object Discard : FilterResult()
    object Pause : FilterResult()
    object Resume : FilterResult()
}

class StationaryFilter(private val config: FilterConfig) {
    private var lastAccepted: LocationPoint? = null
    private var lastValidMs: Long = 0L
    private var isPaused: Boolean = false

    fun process(point: LocationPoint, nowMs: Long = System.currentTimeMillis()): FilterResult {
        val prev = lastAccepted
        if (prev != null && point.distanceTo(prev) < config.minDisplacementM) {
            if (!isPaused && (nowMs - lastValidMs) > config.pauseTimeoutMs) {
                isPaused = true
                return FilterResult.Pause
            }
            return FilterResult.Discard
        }
        val wasResuming = isPaused
        lastAccepted = point
        lastValidMs = nowMs
        isPaused = false
        return if (wasResuming) FilterResult.Resume else FilterResult.Accept(point)
    }

    fun reset() {
        lastAccepted = null
        lastValidMs = 0L
        isPaused = false
    }
}
