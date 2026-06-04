package xyz.northline.overmapper.domain

import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint
import java.text.SimpleDateFormat
import java.util.*

object GpxExporter {
    private val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(trail: Trail, points: List<TrailPoint>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="OverMapper" xmlns="http://www.topografix.com/GPX/1/1">""")
        sb.appendLine("""  <metadata><time>${iso8601.format(Date(trail.recordedAt))}</time></metadata>""")
        sb.appendLine("""  <trk><name>Trail ${trail.id}</name>""")

        val bySegment = points.groupBy { it.segmentIndex }.toSortedMap()
        bySegment.values.forEach { segPts ->
            sb.appendLine("    <trkseg>")
            segPts.forEach { pt ->
                sb.appendLine("""      <trkpt lat="${pt.latitude}" lon="${pt.longitude}">""")
                sb.appendLine("""        <ele>${pt.altitudeM}</ele>""")
                sb.appendLine("""        <time>${iso8601.format(Date(pt.recordedAt))}</time>""")
                sb.appendLine("""      </trkpt>""")
            }
            sb.appendLine("    </trkseg>")
        }

        sb.appendLine("  </trk>")
        sb.appendLine("</gpx>")
        return sb.toString()
    }
}
