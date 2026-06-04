package xyz.northline.overmapper.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import xyz.northline.overmapper.domain.model.TrailPoint
import xyz.northline.overmapper.ui.theme.Terracotta

@Composable
fun ElevationProfileChart(points: List<TrailPoint>, modifier: Modifier = Modifier) {
    if (points.size < 2) return
    val alts = points.map { it.altitudeM.toFloat() }
    val minAlt = alts.min()
    val maxAlt = alts.max()
    val range = (maxAlt - minAlt).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        drawElevationFill(alts, minAlt, range)
        drawElevationLine(alts, minAlt, range)
    }
}

private fun DrawScope.drawElevationFill(alts: List<Float>, minAlt: Float, range: Float) {
    val path = Path()
    val w = size.width; val h = size.height
    path.moveTo(0f, h)
    alts.forEachIndexed { i, alt ->
        val x = w * i / (alts.size - 1)
        val y = h - h * (alt - minAlt) / range
        path.lineTo(x, y)
    }
    path.lineTo(w, h); path.close()
    drawPath(path, Terracotta.copy(alpha = 0.2f))
}

private fun DrawScope.drawElevationLine(alts: List<Float>, minAlt: Float, range: Float) {
    val w = size.width; val h = size.height
    for (i in 0 until alts.size - 1) {
        val x1 = w * i / (alts.size - 1)
        val y1 = h - h * (alts[i] - minAlt) / range
        val x2 = w * (i + 1) / (alts.size - 1)
        val y2 = h - h * (alts[i + 1] - minAlt) / range
        drawLine(Terracotta, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.dp.toPx())
    }
}
