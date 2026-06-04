package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.FilterConfig
import xyz.northline.overmapper.domain.FilterResult
import xyz.northline.overmapper.domain.LocationPoint
import xyz.northline.overmapper.domain.StationaryFilter

class StationaryFilterTest {

    private fun pt(lat: Double, lon: Double, t: Long = 0L) =
        LocationPoint(lat, lon, 0.0, t)

    @Test
    fun `first point is always accepted`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        val result = filter.process(pt(51.0, -0.1), nowMs = 0L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }

    @Test
    fun `point under threshold is discarded`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        // ~2m away
        val result = filter.process(pt(51.000018, -0.1), nowMs = 1_000L)
        assertThat(result).isEqualTo(FilterResult.Discard)
    }

    @Test
    fun `point over threshold is accepted`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        // ~100m north
        val result = filter.process(pt(51.0009, -0.1), nowMs = 5_000L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }

    @Test
    fun `stationary beyond timeout triggers pause`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        val result = filter.process(pt(51.000018, -0.1), nowMs = 61_000L)
        assertThat(result).isEqualTo(FilterResult.Pause)
    }

    @Test
    fun `valid point after pause triggers resume`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        filter.process(pt(51.000018, -0.1), nowMs = 61_000L) // Pause
        val result = filter.process(pt(51.0009, -0.1), nowMs = 70_000L)
        assertThat(result).isEqualTo(FilterResult.Resume)
    }

    @Test
    fun `reset clears state — next point accepted`() {
        val filter = StationaryFilter(FilterConfig())
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        filter.reset()
        val result = filter.process(pt(51.0, -0.1), nowMs = 1_000L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }
}
