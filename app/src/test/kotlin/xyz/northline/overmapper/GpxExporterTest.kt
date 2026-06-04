package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.GpxExporter
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint

class GpxExporterTest {

    private val trail = Trail(1L, 1_700_000_000_000L, 5000f, 3_600_000L, 100f, 350f,
        50.9, -0.5, 51.1, -0.1)

    private fun pt(seg: Int, lat: Double, lon: Double, alt: Double, t: Long) =
        TrailPoint(0L, 1L, seg, lat, lon, alt, t)

    @Test
    fun `output is valid GPX 1_1 with correct namespace`() {
        val gpx = GpxExporter.export(trail, emptyList())
        assertThat(gpx).contains("xmlns=\"http://www.topografix.com/GPX/1/1\"")
        assertThat(gpx).contains("<gpx ")
        assertThat(gpx).contains("</gpx>")
    }

    @Test
    fun `single segment produces one trkseg`() {
        val points = listOf(
            pt(0, 51.0, -0.1, 100.0, 1_700_000_000_000L),
            pt(0, 51.001, -0.1, 102.0, 1_700_000_010_000L)
        )
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx.occurrencesOf("<trkseg>")).isEqualTo(1)
        assertThat(gpx.occurrencesOf("<trkpt")).isEqualTo(2)
    }

    @Test
    fun `segment break produces two trksegs`() {
        val points = listOf(
            pt(0, 51.0, -0.1, 100.0, 1_700_000_000_000L),
            pt(1, 51.005, -0.1, 110.0, 1_700_000_120_000L)
        )
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx.occurrencesOf("<trkseg>")).isEqualTo(2)
    }

    @Test
    fun `elevation and timestamp are included`() {
        val points = listOf(pt(0, 51.0, -0.1, 123.4, 1_700_000_000_000L))
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx).contains("<ele>123.4</ele>")
        assertThat(gpx).contains("<time>")
    }

    private fun String.occurrencesOf(sub: String): Int {
        var count = 0; var idx = 0
        while (true) { idx = indexOf(sub, idx); if (idx == -1) break; count++; idx++ }
        return count
    }
}
