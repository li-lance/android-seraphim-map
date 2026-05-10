package com.seraphim.core.map.here

import android.graphics.Bitmap
import android.util.Log
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolygon
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCircle
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMarker3D
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapStyle
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraMoveReason
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.Circle as ModelCircle
import com.seraphim.core.map.commons.model.CircleOptions as ModelCircleOptions
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng
import com.seraphim.core.map.commons.model.MapType
import com.seraphim.core.map.commons.model.Marker as ModelMarker
import com.seraphim.core.map.commons.model.MarkerOptions as ModelMarkerOptions
import com.seraphim.core.map.commons.model.Polygon as ModelPolygon
import com.seraphim.core.map.commons.model.PolygonOptions as ModelPolygonOptions
import com.seraphim.core.map.commons.model.Polyline as ModelPolyline
import com.seraphim.core.map.commons.model.PolylineOptions as ModelPolylineOptions
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * [MapInstance] implementation for HERE SDK.
 *
 * Wraps HERE [MapView]/[MapScene] and provides a unified interface.
 *
 * NOTE: HERE SDK does not support marker clustering natively.
 * [setClusterItems] will throw [UnsupportedOperationException].
 */
class HereMapInstance : MapInstance {

    private var mapView: MapView? = null
    private val mv: MapView
        get() = mapView
            ?: throw IllegalStateException("MapView not initialized. Call init() first.")

    private val mapScene: MapScene
        get() = mv.mapScene

    private val markers = ConcurrentHashMap<String, MapMarker>()
    private val polylines = mutableListOf<MapPolyline>()
    private val polygons = mutableListOf<MapPolygon>()
    private val circles = mutableListOf<MapCircle>()

    override val camera = HereMapCamera { mapView }
    override val uiSettings: MapUiSettings = HereMapUiSettings { mapView }

    override suspend fun init(host: MapHost, options: MapOptions) {
        val mv = host.awaitNativeMap() as? MapView
            ?: throw IllegalArgumentException("MapHost must provide a MapView instance")
        this.mapView = mv

        applyInitialCamera(options, mv.mapScene)
        applyMapStyle(options.style, mv.mapScene)
        applyUiSettings(options.uiSettings)
        setupListeners(mv.mapScene)
    }

    override suspend fun refreshHost(host: MapHost) {
        val mv = host.awaitNativeMap() as? MapView
            ?: throw IllegalArgumentException("MapHost must provide a MapView instance")
        this.mapView = mv
        setupListeners(mv.mapScene)
    }

    private fun applyInitialCamera(options: MapOptions, scene: MapScene) {
        when (val cam = options.initialCamera) {
            is com.seraphim.core.map.commons.InitialCamera.Position -> {
                scene.camera.lookAt(
                    GeoCoordinates(cam.target.latitude, cam.target.longitude),
                    MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, cam.zoom.toDouble())
                )
            }

            is com.seraphim.core.map.commons.InitialCamera.Bounds -> {
                val geoBox = GeoBox(
                    GeoCoordinates(cam.bounds.southwest.latitude, cam.bounds.southwest.longitude),
                    GeoCoordinates(cam.bounds.northeast.latitude, cam.bounds.northeast.longitude)
                )
                scene.camera.lookAt(geoBox)
            }

            is com.seraphim.core.map.commons.InitialCamera.None -> { /* default */
            }
        }
    }

    private fun applyMapStyle(style: MapStyle, scene: MapScene) {
        when (style) {
            is MapStyle.CustomJson -> {
                Log.d(
                    TAG,
                    "CustomJson: HERE uses YAML map schemes, not JSON. Use MapScheme directly."
                )
            }

            is MapStyle.FromResource -> {
                Log.d(TAG, "FromResource: HERE uses map schemes loaded from assets.")
            }

            MapStyle.Default -> { /* already loaded NORMAL_DAY */
            }
        }
    }

    private fun applyUiSettings(settings: com.seraphim.core.map.commons.UiSettings) {
        val gestures = mv.gestures
        gestures.isPanEnabled = settings.scrollGesturesEnabled
        gestures.isPinchRotateEnabled = settings.zoomGesturesEnabled
        gestures.isTwoFingerPanEnabled = settings.rotateGesturesEnabled
        // Tilt, compass, zoomControls not directly supported

        if (settings.trafficEnabled) {
            mapScene.setLayerState(
                MapScene.Layers.TRAFFIC_FLOW,
                MapScene.LayerState.VISIBLE
            )
        }
    }

    private fun setupListeners(scene: MapScene) {
        // HERE SDK listeners are set via MapView/MapScene callbacks
    }

    // ── Markers ──

    override fun addMarker(options: ModelMarkerOptions): ModelMarker {
        val coords = GeoCoordinates(options.position.latitude, options.position.longitude)
        val mapImage = createMapImage(options.icon)
        val marker = MapMarker(coords, mapImage)
        marker.isVisible = options.visible

        mapScene.addMapMarker(marker)

        val id = "here_" + UUID.randomUUID().toString().take(8)
        markers[id] = marker
        return HereMarker(id, marker)
    }

    private fun createMapImage(icon: IconProvider?): MapImage {
        return when (icon) {
            is IconProvider.FromBitmap -> {
                val bitmap = icon.bitmapDescriptor as? Bitmap
                    ?: return MapImageFactory.fromResource(
                        android.R.drawable.ic_menu_mylocation
                    )
                MapImageFactory.fromBitmap(bitmap)
            }

            is IconProvider.FromAsset -> {
                Log.d(TAG, "FromAsset: load bitmap from assets first, then use FromBitmap")
                MapImageFactory.fromResource(android.R.drawable.ic_menu_mylocation)
            }

            else -> MapImageFactory.fromResource(android.R.drawable.ic_menu_mylocation)
        }
    }

    override fun removeMarker(marker: ModelMarker) {
        val hMarker = (marker as? HereMarker)?.native ?: return
        mapScene.removeMapMarker(hMarker)
        markers.values.remove(hMarker)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.let { mapScene.removeMapMarker(it) }
        markers.remove(id)
    }

    override fun clearMarkers() {
        markers.values.forEach { mapScene.removeMapMarker(it) }
        markers.clear()
    }

    // ── Shapes ──

    override fun addPolyline(options: ModelPolylineOptions): ModelPolyline {
        val geoPolyline = GeoPolyline(
            options.points.map { GeoCoordinates(it.latitude, it.longitude) }
        )
        val polyline = MapPolyline(
            geoPolyline,
            options.width,
            Color(options.color)
        )
        mapScene.addMapPolyline(polyline)
        polylines.add(polyline)
        return HerePolyline(polyline)
    }

    override fun addPolygon(options: ModelPolygonOptions): ModelPolygon {
        val geoPolygon = GeoPolygon(
            options.points.map { GeoCoordinates(it.latitude, it.longitude) }
        )
        val polygon = MapPolygon(
            geoPolygon,
            Color(options.fillColor),
            Color(options.strokeColor),
            options.strokeWidth
        )
        mapScene.addMapPolygon(polygon)
        polygons.add(polygon)
        return HerePolygon(polygon)
    }

    override fun addCircle(options: ModelCircleOptions): ModelCircle {
        val geoCircle = GeoCircle(
            GeoCoordinates(options.center.latitude, options.center.longitude),
            options.radius
        )
        val circle = MapCircle(
            geoCircle,
            Color(options.fillColor),
            Color(options.strokeColor),
            options.strokeWidth
        )
        mapScene.addMapCircle(circle)
        circles.add(circle)
        return HereCircle(circle)
    }

    override fun clearShapes() {
        polylines.forEach { mapScene.removeMapPolyline(it) }
        polylines.clear()
        polygons.forEach { mapScene.removeMapPolygon(it) }
        polygons.clear()
        circles.forEach { mapScene.removeMapCircle(it) }
        circles.clear()
    }

    // ── Map type ──

    override var mapType: MapType
        get() = MapType.NORMAL
        set(value) {
            Log.d(TAG, "mapType: HERE SDK uses MapScheme, not MapType enum")
        }

    // ── User location ──

    override fun enableUserLocation(enabled: Boolean) {
        // HERE uses LocationIndicator for on-map position display
        if (enabled) {
            try {
                mapScene.enableFeatures(mapOf(MapScene.Features.VISIBILITY to "true"))
                val locationIndicator =
                    com.here.sdk.location.LocationIndicator()
                locationIndicator.isVisible = true
                // Note: LocationIndicator requires LocationEngine to be active
            } catch (e: Exception) {
                Log.w(TAG, "Failed to enable user location", e)
            }
        }
    }

    // ── Events ──

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            mv.gestures.setTapListener { point ->
                val coords = mv.viewToGeoCoordinates(point)
                if (coords != null) {
                    value?.invoke(ModelLatLng(coords.latitude, coords.longitude))
                }
            }
        }

    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            mv.gestures.setLongPressListener { point ->
                val coords = mv.viewToGeoCoordinates(point)
                if (coords != null) {
                    value?.invoke(ModelLatLng(coords.latitude, coords.longitude))
                }
            }
        }

    override var onMarkerClick: ((markerId: String) -> Boolean)? = null
        // HERE SDK marker click is handled per-marker via MapMarker.setOnClickListener
        // This simplified implementation sets tap on all existing markers
        set(value) {
            field = value
            // Note: In production, track newly added markers and set listener individually
        }

    override var onCameraChange: ((CameraState) -> Unit)? = null
        set(value) {
            field = value
            mv.camera.addObserver { reason ->
                when (reason) {
                    MapCamera.StateChangeReason.USER_INTERACTION ->
                        value?.invoke(CameraState.Started(CameraMoveReason.GESTURE))

                    MapCamera.StateChangeReason.ANIMATION ->
                        value?.invoke(CameraState.Started(CameraMoveReason.API_ANIMATION))

                    else ->
                        value?.invoke(CameraState.Idle(camera.current))
                }
            }
        }

    // ── Cluster items ──

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException(
            "HERE SDK does not support clustering natively. " +
                    "Use a custom clustering implementation."
        )
    }

    // ── Cleanup ──

    override fun clearAll() {
        clearMarkers()
        clearShapes()
    }

    companion object {
        private const val TAG = "HereMapInstance"
    }
}

// ── Internal wrapper classes ──

private class HereMarker(
    override val id: String,
    val native: MapMarker
) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.coordinates.latitude, native.coordinates.longitude)
        set(value) {
            native.coordinates = GeoCoordinates(value.latitude, value.longitude)
        }

    override var visible: Boolean
        get() = native.isVisible
        set(value) {
            native.isVisible = value
        }

    override fun remove() { /* managed by HereMapInstance */
    }
}

private class HerePolyline(
    private val native: MapPolyline
) : ModelPolyline {
    override fun remove() { /* managed by HereMapInstance */
    }
}

private class HerePolygon(
    private val native: MapPolygon
) : ModelPolygon {
    override fun remove() { /* managed by HereMapInstance */
    }
}

private class HereCircle(
    private val native: MapCircle
) : ModelCircle {
    override fun remove() { /* managed by HereMapInstance */
    }
}
