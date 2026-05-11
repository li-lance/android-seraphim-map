package com.seraphim.core.map.tmap

import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.UiSettings
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.MapType
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapCircle
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.overlay.TMapPolyLine
import com.skt.tmap.overlay.TMapPolygon
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

open class TmapMapInstance : MapInstance {

    private var mapView: TMapView? = null
    private val mv: TMapView get() = mapView ?: throw IllegalStateException("Not initialized")

    private val markers = ConcurrentHashMap<String, TMapMarkerItem>()
    private val polylines = mutableListOf<TMapPolyLine>()
    private val polygons = mutableListOf<TMapPolygon>()
    private val circles = mutableListOf<TMapCircle>()

    override val camera = TmapMapCamera { mapView }
    private var _uiSettings = UiSettings()
    override val uiSettings: UiSettings get() = _uiSettings

    override fun updateUiSettings(settings: UiSettings) {
        _uiSettings = settings
        applyUiSettings(settings)
    }

    private fun applyUiSettings(settings: UiSettings) {
        // TODO: apply to native map for tmap
    }

    override suspend fun init(host: MapHost, options: MapOptions) {
        val mv = host.awaitNativeMap() as? TMapView
            ?: throw IllegalArgumentException("MapHost must provide a TMapView")
        this.mapView = mv
        applyMapType(options.mapType)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        this.mapView = host.awaitNativeMap() as? TMapView
            ?: throw IllegalArgumentException("MapHost must provide a TMapView")
    }

    private fun applyMapType(type: MapType) {
        mv.setMapType(
            when (type) {
                MapType.NORMAL -> TMapView.MapType.DEFAULT
                MapType.SATELLITE -> TMapView.MapType.SATELLITE
                MapType.HYBRID -> TMapView.MapType.PUBLIC
                else -> TMapView.MapType.DEFAULT
            }
        )
    }

    private fun setupListeners() {}

    // ── Markers ──
    override fun addMarker(options: ModelMarkerOptions): ModelMarker {
        val marker = TMapMarkerItem().apply {
            tMapPoint = TMapPoint(options.position.latitude, options.position.longitude)
            canShowCallout = true
            calloutTitle = options.title
            calloutSubTitle = options.snippet
            visible = options.visible
        }
        val id = "tmap_" + UUID.randomUUID().toString().take(8)
        marker.id = id
        mv.addTMapMarkerItem(marker)
        markers[id] = marker
        return TmapMarker(id, marker)
    }

    override fun removeMarker(marker: ModelMarker) {
        val tm = (marker as? TmapMarker)?.native ?: return
        mv.removeTMapMarkerItem(tm.id)
        markers.remove(marker.id)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.let { mv.removeTMapMarkerItem(it.id) }
        markers.remove(id)
    }

    override fun clearMarkers() {
        mv.removeAllTMapMarkerItem()
        markers.clear()
    }

    // ── Shapes ──
    override fun addPolyline(options: ModelPolylineOptions): ModelPolyline {
        val pl = TMapPolyLine().apply {
            options.points.forEach { addLinePoint(TMapPoint(it.latitude, it.longitude)) }
            lineColor = options.color
            // lineWidth set via property in older TMap SDK
        }
        mv.addTMapPolyLine(pl)
        polylines.add(pl)
        return TmapPolyline(pl)
    }

    override fun addPolygon(options: ModelPolygonOptions): ModelPolygon {
        val pg = TMapPolygon().apply {
            options.points.forEach { addPolygonPoint(TMapPoint(it.latitude, it.longitude)) }
            areaColor = options.fillColor
            lineColor = options.strokeColor
            // strokeWidth set via TMapCircle property
        }
        mv.addTMapPolygon(pg)
        polygons.add(pg)
        return TmapPolygon(pg)
    }

    override fun addCircle(options: ModelCircleOptions): ModelCircle {
        val c = TMapCircle().apply {
            centerPoint = TMapPoint(options.center.latitude, options.center.longitude)
            radius = options.radius
            areaColor = options.fillColor
            lineColor = options.strokeColor
            // strokeWidth set via TMapCircle property
        }
        mv.addTMapCircle(c)
        circles.add(c)
        return TmapCircle(c)
    }

    override fun clearShapes() {
        mv.removeAllTMapPolyLine()
        mv.removeAllTMapPolygon()
        mv.removeAllTMapCircle()
        polylines.clear(); polygons.clear(); circles.clear()
    }


    override fun enableUserLocation(enabled: Boolean) {
        mv.setIconVisibility(enabled)
    }

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
        set(v) {
            field = v
            mv.setOnClickListenerCallback(object : TMapView.OnClickListenerCallback {
                override fun onPressDown(
                    markers: ArrayList<TMapMarkerItem>?,
                    pois: ArrayList<com.skt.tmap.poi.TMapPOIItem>?,
                    point: TMapPoint?,
                    screenPoint: android.graphics.PointF?
                ) {
                    point?.let { v?.invoke(ModelLatLng(it.latitude, it.longitude)) }
                }

                override fun onPressUp(
                    markers: ArrayList<TMapMarkerItem>?,
                    pois: ArrayList<com.skt.tmap.poi.TMapPOIItem>?,
                    point: TMapPoint?,
                    screenPoint: android.graphics.PointF?
                ) {
                }
            })
        }

    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
    override var onMarkerClick: ((String) -> Boolean)? = null

    override var onCameraChange: ((CameraState) -> Unit)? = null
        set(v) {
            field = v
            mv.setOnPanChangedListener {
                v?.invoke(CameraState.Moving(camera.current))
            }
        }

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException("Use TmapClusterableMap for clustering.")
    }

    override fun clearAll() {
        clearShapes()
        clearMarkers()
    }

    companion object {
        private const val TAG = "TmapMapInstance"
    }
}

private class TmapMarker(override val id: String, val native: TMapMarkerItem) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.tMapPoint.latitude, native.tMapPoint.longitude)
        set(v) {
            native.tMapPoint = TMapPoint(v.latitude, v.longitude)
        }
    override var visible: Boolean
        get() = native.visible
        set(v) {
            native.visible = v
        }
    override var tag: Any?
        get() = null // TMapMarkerItem.id is String, cannot store Any
        set(value) { /* store in wrapper if needed */ }

    override fun remove() {}
}

private class TmapPolyline(val native: TMapPolyLine) : ModelPolyline {
    override fun remove() {}
}

private class TmapPolygon(val native: TMapPolygon) : ModelPolygon {
    override fun remove() {}
}

private class TmapCircle(val native: TMapCircle) : ModelCircle {
    override fun remove() {}
}
