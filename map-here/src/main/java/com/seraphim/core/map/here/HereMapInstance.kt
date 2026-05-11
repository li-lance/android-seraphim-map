package com.seraphim.core.map.here

import android.graphics.Bitmap
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.Circle
import com.seraphim.core.map.commons.model.CircleOptions
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.MapType
import com.seraphim.core.map.commons.model.Marker
import com.seraphim.core.map.commons.model.MarkerOptions
import com.seraphim.core.map.commons.model.Polygon
import com.seraphim.core.map.commons.model.PolygonOptions
import com.seraphim.core.map.commons.model.Polyline
import com.seraphim.core.map.commons.model.PolylineOptions
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.here.sdk.mapview.MapScene as HereMapScene

open class HereMapInstance : MapInstance {

    private var mapView: MapView? = null
    private var scene: HereMapScene? = null
    private val mv: MapView get() = mapView ?: throw IllegalStateException("Not initialized")
    private val ms: HereMapScene get() = scene ?: throw IllegalStateException("Not initialized")

    private val markers = ConcurrentHashMap<String, MapMarker>()
    private val polylines = mutableListOf<MapPolyline>()
    private val polygons = mutableListOf<MapPolygon>()

    override val camera = HereMapCamera(mv)
    override val uiSettings: MapUiSettings = HereMapUiSettings { scene }

    override suspend fun init(host: MapHost, opts: MapOptions) {
        val native = host.awaitNativeMap() as? MapView
            ?: throw IllegalArgumentException("Expected MapView")
        mapView = native
        scene = native.mapScene
        applyMapType(opts.mapType)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        mapView = host.awaitNativeMap() as? MapView
        scene = mapView?.mapScene
    }

    private fun applyMapType(type: MapType) {
        ms.loadScene(
            when (type) {
                MapType.NORMAL -> MapScheme.NORMAL_DAY
                MapType.SATELLITE -> MapScheme.SATELLITE
                else -> MapScheme.NORMAL_DAY
            }, null
        )
    }

    private fun setupListeners() {
        // TODO: HERE 4.25.5 gesture API differs. Verify TapListener/LongPressListener signatures.
    }

    override fun addMarker(opts: MarkerOptions): Marker {
        val icon = opts.icon
        val image = when {
            icon is IconProvider.FromBitmap -> {
                val bmp = (icon as IconProvider.FromBitmap).bitmapDescriptor as? Bitmap
                if (bmp != null) MapImageFactory.fromBitmap(bmp)
                else MapImageFactory.fromResource(
                    mv.context.resources,
                    android.R.drawable.ic_dialog_map
                )
            }

            else -> MapImageFactory.fromResource(
                mv.context.resources,
                android.R.drawable.ic_dialog_map
            )
        }
        val anchor = Anchor2D(opts.anchor.first.toDouble(), opts.anchor.second.toDouble())
        val m = MapMarker(
            GeoCoordinates(opts.position.latitude, opts.position.longitude),
            image,
            anchor
        )
        ms.addMapMarker(m)
        val id = "here_" + UUID.randomUUID().toString().take(8)
        markers[id] = m
        return HereMarker(id, m)
    }

    override fun removeMarker(marker: Marker) {
        (marker as? HereMarker)?.native?.let { ms.removeMapMarker(it) }
        markers.remove(marker.id)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.let { ms.removeMapMarker(it) }; markers.remove(id)
    }

    override fun clearMarkers() {
        markers.values.forEach { ms.removeMapMarker(it) }; markers.clear()
    }

    override fun addPolyline(opts: PolylineOptions): Polyline {
        // TODO: HERE 4.25.5 MapPolyline constructor requires Representation.
        // In Android Studio, Ctrl+P on MapPolyline to see constructor options.
        // Representation can be created via MapPolyline.Representation subclass.
        return object : Polyline {
            override fun remove() {}
        }
    }

    override fun addPolygon(opts: PolygonOptions): Polygon {
        val pts = opts.points.map { GeoCoordinates(it.latitude, it.longitude) }
        val r = (opts.fillColor shr 16 and 0xFF) / 255f
        val g = (opts.fillColor shr 8 and 0xFF) / 255f
        val b = (opts.fillColor and 0xFF) / 255f
        val a = (opts.fillColor shr 24 and 0xFF) / 255f
        val pg = MapPolygon(GeoPolygon(pts), Color(r, g, b, a))
        ms.addMapPolygon(pg)
        polygons.add(pg)
        return HerePolygon(pg)
    }

    override fun addCircle(opts: CircleOptions): Circle {
        // HERE doesn't have native Circle — approximate with polygon
        val center = GeoCoordinates(opts.center.latitude, opts.center.longitude)
        val points = (0..35).map { i ->
            val angle = 2 * Math.PI * i / 36
            val lat = center.latitude + (opts.radius / 111320.0) * Math.cos(angle)
            val lng =
                center.longitude + (opts.radius / (111320.0 * Math.cos(Math.toRadians(center.latitude)))) * Math.sin(
                    angle
                )
            GeoCoordinates(lat, lng)
        }
        val pg = MapPolygon(GeoPolygon(points), Color(0f, 1f, 0f, 0.4f))
        ms.addMapPolygon(pg)
        polygons.add(pg)
        return object : Circle {
            override fun remove() {
                ms.removeMapPolygon(pg)
            }
        }
    }

    override fun clearShapes() {
        polylines.forEach { ms.removeMapPolyline(it) }; polylines.clear()
        polygons.forEach { ms.removeMapPolygon(it) }; polygons.clear()
    }

    override var mapType: MapType = MapType.NORMAL;
        set(v) {
            applyMapType(v)
        }

    override fun enableUserLocation(enabled: Boolean) { /* TODO: LocationIndicator */
    }

    override var onMapClick: ((LatLng) -> Unit)? = null
    override var onMapLongClick: ((LatLng) -> Unit)? = null
    override var onMarkerClick: ((String) -> Boolean)? = null
    override var onCameraChange: ((CameraState) -> Unit)? = null

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException("Use HereClusterableMap for clustering.")
    }

    override fun clearAll() {
        clearMarkers(); clearShapes()
    }
}

private class HereMarker(override val id: String, val native: MapMarker) : Marker {
    override var position: LatLng
        get() {
            val c = native.coordinates
            return LatLng(c.latitude, c.longitude)
        }
        set(v) { /* immutable */ }
    override var visible: Boolean = true
    override fun remove() { /* caller handles removal */
    }
}

private class HerePolyline(val native: MapPolyline) : Polyline {
    override fun remove() { /* handled */
    }
}

private class HerePolygon(val native: MapPolygon) : Polygon {
    override fun remove() { /* handled */
    }
}
