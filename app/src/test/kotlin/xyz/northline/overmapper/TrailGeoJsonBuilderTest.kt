package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.TrailGeoJsonBuilder
import xyz.northline.overmapper.domain.model.TrailPoint

class TrailGeoJsonBuilderTest {

    private fun pt(trailId: Long, seg: Int, lat: Double, lon: Double, alt: Double = 0.0, t: Long = 0L) =
        TrailPoint(0L, trailId, seg, lat, lon, alt, t)

    @Test
    fun `empty points list returns empty feature collection`() {
        val json = TrailGeoJsonBuilder.buildOverlay(emptyList(), emptyList())
        assertThat(json).contains("\"type\":\"FeatureCollection\"")
        assertThat(json).contains("\"features\":[]")
    }

    @Test
    fun `single trail single segment produces one feature`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1),
            pt(1L, 0, 51.001, -0.1),
            pt(1L, 0, 51.002, -0.1)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(listOf(1L), points)
        assertThat(json.occurrencesOf("\"type\":\"Feature\"")).isEqualTo(1)
    }

    @Test
    fun `segment break produces two features`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1),
            pt(1L, 0, 51.001, -0.1),
            pt(1L, 1, 51.005, -0.1),
            pt(1L, 1, 51.006, -0.1)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(listOf(1L), points)
        assertThat(json.occurrencesOf("\"type\":\"Feature\"")).isEqualTo(2)
    }

    @Test
    fun `newest trail gets new age bucket`() {
        val trailIds = listOf(1L, 2L)
        val points = listOf(
            pt(1L, 0, 51.0, -0.1),
            pt(1L, 0, 51.001, -0.1),
            pt(2L, 0, 51.0, -0.2),
            pt(2L, 0, 51.001, -0.2)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(trailIds, points)
        assertThat(json).contains("\"age_bucket\":\"new\"")
    }

    @Test
    fun `gradient json segments each point pair with slope property`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1, alt = 100.0),
            pt(1L, 0, 51.001, -0.1, alt = 110.0),
            pt(1L, 0, 51.002, -0.1, alt = 108.0)
        )
        val json = TrailGeoJsonBuilder.buildGradient(points)
        assertThat(json).contains("\"slope\":")
        assertThat(json).contains("uphill")
    }

    private fun String.occurrencesOf(sub: String): Int {
        var count = 0; var idx = 0
        while (true) { idx = indexOf(sub, idx); if (idx == -1) break; count++; idx++ }
        return count
    }
}
