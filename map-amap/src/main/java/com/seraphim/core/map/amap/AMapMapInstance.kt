package com.seraphim.core.map.amap

import android.graphics.Bitmap
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CircleOptions
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolygonOptions
import com.amap.api.maps.model.PolylineOptions
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.MapType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.amap.api.maps.model.LatLng as AMapLatLng
import com.seraphim.core.map.commons.model.Circle as ModelCircle
import com.seraphim.core.map.commons.model.CircleOptions as ModelCircleOptions
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng
import com.seraphim.core.map.commons.model.Marker as ModelMarker
import com.seraphim.core.map.commons.model.MarkerOptions as ModelMarkerOptions
import com.seraphim.core.map.commons.model.Polygon as ModelPolygon
import com.seraphim.core.map.commons.model.PolygonOptions as ModelPolygonOptions
import com.seraphim.core.map.commons.model.Polyline as ModelPolyline
import com.seraphim.core.map.commons.model.PolylineOptions as ModelPolylineOptions

open class AMapMapInstance : MapInstance {

    private var aMap: AMap? = null
    private val m: AMap get() = aMap ?: throw IllegalStateException("Not initialized")

    private val markers = ConcurrentHashMap<String, Marker>()
    private val polylines = mutableListOf<com.amap.api.maps.model.Polyline>()
    private val polygons = mutableListOf<com.amap.api.maps.model.Polygon>()
    private val circles = mutableListOf<com.amap.api.maps.model.Circle>()

    override val camera = AMapMapCamera { aMap }
    override val uiSettings: MapUiSettings = AMapMapUiSettings { aMap }

    override suspend fun init(host: MapHost, opts: MapOptions) {
        aMap = host.awaitNativeMap() as? AMap ?: throw IllegalArgumentException("Expected AMap")
        applyMapType(opts.mapType)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        aMap = host.awaitNativeMap() as? AMap
    }

    private fun applyMapType(type: MapType) {
        m.mapType = when (type) {
            MapType.NORMAL -> AMap.MAP_TYPE_NORMAL
            MapType.SATELLITE -> AMap.MAP_TYPE_SATELLITE
            else -> AMap.MAP_TYPE_NORMAL
        }
    }

    private fun setupListeners() {}

    override fun addMarker(opts: ModelMarkerOptions): ModelMarker {
        val mo = MarkerOptions()
            .position(AMapLatLng(opts.position.latitude, opts.position.longitude))
            .title(opts.title).snippet(opts.snippet)
            .alpha(opts.alpha).visible(opts.visible)
            .zIndex(opts.zIndex)
            .anchor(opts.anchor.first, opts.anchor.second)
        when (val icon = opts.icon) {
            is IconProvider.FromBitmap -> {
                val bmp = icon.bitmapDescriptor as? Bitmap
                if (bmp != null) mo.icon(BitmapDescriptorFactory.fromBitmap(bmp))
            }

            else -> {}
        }
        val marker = m.addMarker(mo) ?: throw IllegalStateException("Failed to add marker")
        val id = "amap_" + UUID.randomUUID().toString().take(8)
        marker.`object` = id
        markers[id] = marker
        return AMapMarker(id, marker)
    }

    override fun removeMarker(marker: ModelMarker) {
        (marker as? AMapMarker)?.native?.remove(); markers.remove(marker.id)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.remove(); markers.remove(id)
    }

    override fun clearMarkers() {
        m.clear(); markers.clear()
    }

    override fun addPolyline(opts: ModelPolylineOptions): ModelPolyline {
        val pl = m.addPolyline(
            PolylineOptions()
                .addAll(opts.points.map { AMapLatLng(it.latitude, it.longitude) })
                .color(opts.color).width(opts.width).zIndex(opts.zIndex).visible(opts.visible)
        )
        polylines.add(pl)
        return AMapPolyline(pl)
    }

    override fun addPolygon(opts: ModelPolygonOptions): ModelPolygon {
        val pg = m.addPolygon(
            PolygonOptions()
                .addAll(opts.points.map { AMapLatLng(it.latitude, it.longitude) })
                .fillColor(opts.fillColor).strokeColor(opts.strokeColor)
                .strokeWidth(opts.strokeWidth)
                .zIndex(opts.zIndex).visible(opts.visible)
        )
        polygons.add(pg)
        return AMapPolygon(pg)
    }

    override fun addCircle(opts: ModelCircleOptions): ModelCircle {
        val c = m.addCircle(
            CircleOptions()
                .center(AMapLatLng(opts.center.latitude, opts.center.longitude))
                .radius(opts.radius).fillColor(opts.fillColor).strokeColor(opts.strokeColor)
                .strokeWidth(opts.strokeWidth).zIndex(opts.zIndex).visible(opts.visible)
        )
        circles.add(c)
        return AMapCircle(c)
    }

    override fun clearShapes() {
        polylines.forEach { it.remove() }; polylines.clear()
        polygons.forEach { it.remove() }; polygons.clear()
        circles.forEach { it.remove() }; circles.clear()
    }

    override var mapType: MapType
        get() = when (m.mapType) {
            AMap.MAP_TYPE_NORMAL -> MapType.NORMAL
            AMap.MAP_TYPE_SATELLITE -> MapType.SATELLITE
            else -> MapType.NORMAL
        }
        set(v) {
            applyMapType(v)
        }

    override fun enableUserLocation(enabled: Boolean) {
        m.isMyLocationEnabled = enabled
    }

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
        set(v) {
            field = v; m.setOnMapClickListener { v?.invoke(ModelLatLng(it.latitude, it.longitude)) }
        }
    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
        set(v) {
            field = v; m.setOnMapLongClickListener {
                v?.invoke(
                    ModelLatLng(
                        it.latitude,
                        it.longitude
                    )
                )
            }
        }
    override var onMarkerClick: ((String) -> Boolean)? = null
        set(v) {
            field = v; m.setOnMarkerClickListener {
                v?.invoke((it.`object` as? String) ?: "") ?: false
            }
        }
    override var onCameraChange: ((CameraState) -> Unit)? = null
        set(v) {
            field = v
            m.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                override fun onCameraChange(p0: com.amap.api.maps.model.CameraPosition?) {}
                override fun onCameraChangeFinish(p0: com.amap.api.maps.model.CameraPosition?) {
                    v?.invoke(CameraState.Idle(camera.current))
                }
            })
        }

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException("Use AMapClusterableMap for clustering.")
    }

    override fun clearAll() {
        clearMarkers(); clearShapes()
    }

    companion object {
        private const val TAG = "AMapMapInstance"
    }
}

private class AMapMarker(override val id: String, val native: Marker) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.position.latitude, native.position.longitude)
        set(v) {
            native.position = AMapLatLng(v.latitude, v.longitude)
        }
    override var visible: Boolean
        get() = native.isVisible;
        set(v) {
            native.isVisible = v
        }

    override fun remove() {
        native.remove()
    }
}

private class AMapPolyline(val native: com.amap.api.maps.model.Polyline) : ModelPolyline {
    override fun remove() {
        native.remove()
    }
}

private class AMapPolygon(val native: com.amap.api.maps.model.Polygon) : ModelPolygon {
    override fun remove() {
        native.remove()
    }
}

private class AMapCircle(val native: com.amap.api.maps.model.Circle) : ModelCircle {
    override fun remove() {
        native.remove()
    }
}
