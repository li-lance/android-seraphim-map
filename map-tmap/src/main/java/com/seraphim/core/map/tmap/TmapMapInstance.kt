package com.seraphim.core.map.tmap

import android.graphics.Bitmap
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.MapType
import com.skt.Tmap.TMapCircle
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.seraphim.core.map.commons.model.Circle as ModelCircle
import com.seraphim.core.map.commons.model.CircleOptions as ModelCircleOptions
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng
import com.seraphim.core.map.commons.model.Marker as ModelMarker
import com.seraphim.core.map.commons.model.MarkerOptions as ModelMarkerOptions
import com.seraphim.core.map.commons.model.Polygon as ModelPolygon
import com.seraphim.core.map.commons.model.PolygonOptions as ModelPolygonOptions
import com.seraphim.core.map.commons.model.Polyline as ModelPolyline
import com.seraphim.core.map.commons.model.PolylineOptions as ModelPolylineOptions

class TmapMapInstance : MapInstance {

    private var mapView: TMapView? = null
    private val mv: TMapView
        get() = mapView ?: throw IllegalStateException("TMapView not initialized")

    private val markers = ConcurrentHashMap<String, TMapMarkerItem>()
    private val polylines = mutableListOf<TMapPolyLine>()
    private val circles = mutableListOf<TMapCircle>()

    override val camera = TmapMapCamera { mapView }
    override val uiSettings: MapUiSettings = TmapMapUiSettings { mapView }

    override suspend fun init(host: MapHost, options: MapOptions) {
        val mv = host.awaitNativeMap() as? TMapView
            ?: throw IllegalArgumentException("MapHost must provide a TMapView")
        this.mapView = mv

        applyMapType(options.mapType)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        val mv = host.awaitNativeMap() as? TMapView
            ?: throw IllegalArgumentException("MapHost must provide a TMapView")
        this.mapView = mv
    }

    private fun applyMapType(type: MapType) {
        mv.setMapType(
            when (type) {
                MapType.NORMAL -> TMapView.MAPTYPE_STANDARD
                MapType.SATELLITE -> TMapView.MAPTYPE_SATELLITE
                else -> TMapView.MAPTYPE_STANDARD
            }
        )
    }

    private fun setupListeners() {
        mv.setOnClickListenerCallback { point ->
            onMapClick?.invoke(ModelLatLng(point.latitude, point.longitude))
        }
        mv.setOnLongClickListenerCallback { point ->
            onMapLongClick?.invoke(ModelLatLng(point.latitude, point.longitude))
        }
    }

    override fun addMarker(options: ModelMarkerOptions): ModelMarker {
        val point = TMapPoint(options.position.latitude, options.position.longitude)
        val marker = TMapMarkerItem()
        marker.tMapPoint = point
        marker.canShowCallout = true
        marker.calloutTitle = options.title
        marker.calloutSubTitle = options.snippet
        marker.visible = options.visible

        options.icon?.let { icon ->
            when (icon) {
                is IconProvider.FromBitmap -> {
                    val bitmap = icon.bitmapDescriptor as? Bitmap
                    if (bitmap != null) marker.icon = bitmap
                }

                else -> {}
            }
        }

        mv.addMarkerItem("m_" + UUID.randomUUID().toString().take(8), marker)
        val id = "tmap_" + UUID.randomUUID().toString().take(8)
        markers[id] = marker
        return TmapMarker(id, marker)
    }

    override fun removeMarker(marker: ModelMarker) {
        val tm = (marker as? TmapMarker)?.native ?: return
        mv.removeMarkerItem(tm.id)
        markers.values.remove(tm)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.let { mv.removeMarkerItem(it.id) }
        markers.remove(id)
    }

    override fun clearMarkers() {
        markers.values.forEach { mv.removeMarkerItem(it.id) }
        markers.clear()
    }

    override fun addPolyline(options: ModelPolylineOptions): ModelPolyline {
        val polyline = TMapPolyLine().apply {
            options.points.forEach { p ->
                addLinePoint(TMapPoint(p.latitude, p.longitude))
            }
            lineColor = options.color
            lineWidth = options.width
        }
        mv.addTMapPolyLine("pl_" + UUID.randomUUID().toString().take(8), polyline)
        polylines.add(polyline)
        return TmapPolyline(polyline)
    }

    override fun addPolygon(options: ModelPolygonOptions): ModelPolygon {
        // Tmap SDK doesn't have native polygon support.
        // Fallback: render as polyline (closed loop)
        val polyline = TMapPolyLine().apply {
            val pts = options.points.toMutableList()
            if (pts.isNotEmpty()) pts.add(pts.first())
            pts.forEach { p -> addLinePoint(TMapPoint(p.latitude, p.longitude)) }
            lineColor = options.fillColor
            lineWidth = options.strokeWidth
        }
        mv.addTMapPolyLine("pg_" + UUID.randomUUID().toString().take(8), polyline)
        return TmapPolygon(polyline)
    }

    override fun addCircle(options: ModelCircleOptions): ModelCircle {
        val circle = TMapCircle().apply {
            centerPoint = TMapPoint(options.center.latitude, options.center.longitude)
            radius = options.radius
            areaColor = options.fillColor
            lineColor = options.strokeColor
            lineWidth = options.strokeWidth
        }
        mv.addTMapCircle("c_" + UUID.randomUUID().toString().take(8), circle)
        circles.add(circle)
        return TmapCircle(circle)
    }

    override fun clearShapes() {
        polylines.forEach { mv.removeTMapPolyLine(it.id) }
        polylines.clear()
        circles.forEach { mv.removeTMapCircle(it.id) }
        circles.clear()
    }

    override var mapType: MapType
        get() = MapType.NORMAL
        set(value) {
            applyMapType(value)
        }

    override fun enableUserLocation(enabled: Boolean) {
        if (enabled) {
            mv.setIconVisibility(true)
        }
    }

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
    override var onMarkerClick: ((String) -> Boolean)? = null
        set(value) {
            field = value
            mv.setOnCalloutRightButtonClickListener { markerId ->
                value?.invoke(markerId) ?: false
            }
        }
    override var onCameraChange: ((CameraState) -> Unit)? = null

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException("Tmap SDK does not support clustering.")
    }

    override fun clearAll() {
        clearMarkers()
        clearShapes()
    }
}

private class TmapMarker(override val id: String, val native: TMapMarkerItem) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.tMapPoint.latitude, native.tMapPoint.longitude)
        set(value) {
            native.tMapPoint = TMapPoint(value.latitude, value.longitude)
        }
    override var visible: Boolean
        get() = native.visible
        set(value) {
            native.visible = value
        }

    override fun remove() {}
}

private class TmapPolyline(private val native: TMapPolyLine) : ModelPolyline {
    override fun remove() {}
}

private class TmapPolygon(private val native: TMapPolyLine) : ModelPolygon {
    override fun remove() {}
}

private class TmapCircle(private val native: TMapCircle) : ModelCircle {
    override fun remove() {}
}
