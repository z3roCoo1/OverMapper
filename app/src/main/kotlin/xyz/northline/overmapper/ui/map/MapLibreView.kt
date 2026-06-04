package xyz.northline.overmapper.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

object TileSource {
    const val OPENFREEMAP = "https://tiles.openfreemap.org/styles/liberty"
    const val OSM_STANDARD = "https://tiles.openfreemap.org/styles/bright"
    const val OSM_HUMANITARIAN = "https://tiles.openfreemap.org/styles/positron"

    fun styleUrl(source: String) = when (source) {
        "OSM_STANDARD" -> OSM_STANDARD
        "OSM_HUMANITARIAN" -> OSM_HUMANITARIAN
        else -> OPENFREEMAP
    }
}

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap, MapView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onCreate(null)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView.also { it.getMapAsync { map -> onMapReady(map, it) } } },
        modifier = modifier
    )
}
