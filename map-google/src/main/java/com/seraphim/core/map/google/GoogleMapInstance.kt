package com.seraphim.core.map.google

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapStyle
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraMoveReason
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.MapType
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

/**
 * [MapInstance] implementation for Google Maps.
 *
 * Wraps [GoogleMap] and provides a unified interface per the commons contract.
 *
 * Internal error handling: Google Play Services availability is checked
 * by [GoogleMapInstanceFactory.checkAvailability] before creation.
 */
open class GoogleMapInstance : MapInstance {

    private var googleMap: GoogleMap? = null
    private val map: GoogleMap
        get() = googleMap
            ?: throw IllegalStateException("GoogleMap not initialized. Call init() first.")

    // Internal storage for markers, shapes
    private val markers = ConcurrentHashMap<String, Marker>()
    private val polylines = mutableListOf<Polyline>()
    private val polygons = mutableListOf<Polygon>()
    private val circles = mutableListOf<Circle>()

    override val camera = GoogleMapCamera { googleMap }
    override val uiSettings: MapUiSettings = GoogleMapUiSettings { googleMap }

    // ── Init ──

    override suspend fun init(host: MapHost, options: MapOptions) {
        googleMap = host.awaitNativeMap() as? GoogleMap
            ?: throw IllegalArgumentException("MapHost must provide a GoogleMap instance")

        applyInitialCamera(options)
        applyMapType(options.mapType)
        applyUiSettings(options.uiSettings)
        applyStyle(options.style)
        applyPadding(options.padding)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        googleMap = host.awaitNativeMap() as? GoogleMap
            ?: throw IllegalArgumentException("MapHost must provide a GoogleMap instance")
    }

    private fun applyInitialCamera(options: MapOptions) {
        when (val cam = options.initialCamera) {
            is com.seraphim.core.map.commons.InitialCamera.Position -> {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(cam.target.latitude, cam.target.longitude),
                        cam.zoom
                    )
                )
            }

            is com.seraphim.core.map.commons.InitialCamera.Bounds -> {
                val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                    .include(LatLng(cam.bounds.southwest.latitude, cam.bounds.southwest.longitude))
                    .include(LatLng(cam.bounds.northeast.latitude, cam.bounds.northeast.longitude))
                    .build()
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, cam.padding))
            }

            is com.seraphim.core.map.commons.InitialCamera.None -> {
                // Let provider use default
            }
        }
    }

    private fun applyMapType(type: MapType) {
        map.mapType = when (type) {
            MapType.NONE -> GoogleMap.MAP_TYPE_NONE
            MapType.NORMAL -> GoogleMap.MAP_TYPE_NORMAL
            MapType.SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
            MapType.HYBRID -> GoogleMap.MAP_TYPE_HYBRID
            MapType.TERRAIN -> GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    private fun applyUiSettings(settings: com.seraphim.core.map.commons.UiSettings) {
        val ui = map.uiSettings
        ui.isScrollGesturesEnabled = settings.scrollGesturesEnabled
        ui.isZoomGesturesEnabled = settings.zoomGesturesEnabled
        ui.isRotateGesturesEnabled = settings.rotateGesturesEnabled
        ui.isTiltGesturesEnabled = settings.tiltGesturesEnabled
        ui.isCompassEnabled = settings.compassEnabled
        ui.isMyLocationButtonEnabled = settings.myLocationButtonEnabled
        ui.isZoomControlsEnabled = settings.zoomControlsEnabled
        ui.isMapToolbarEnabled = settings.mapToolbarEnabled
        map.isTrafficEnabled = settings.trafficEnabled
        map.isIndoorEnabled = settings.indoorEnabled
        map.isBuildingsEnabled = settings.buildingsEnabled
    }

    private fun applyStyle(style: MapStyle) {
        when (style) {
            is MapStyle.CustomJson -> map.setMapStyle(
                com.google.android.gms.maps.model.MapStyleOptions(style.jsonString)
            )

            is MapStyle.FromResource -> {
                // Load raw resource — delegate to caller's context
                Log.d(
                    TAG,
                    "MapStyle.FromResource: styles must be set via JSON string. Use CustomJson for raw resources."
                )
            }

            MapStyle.Default -> { /* do nothing */
            }
        }
    }

    private fun applyPadding(padding: com.seraphim.core.map.commons.model.MapPadding) {
        map.setPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    private fun setupListeners() {
        // Core listeners set up by init; additional ones via properties below
    }

    // ── Markers ──

    override fun addMarker(options: ModelMarkerOptions): ModelMarker {
        val id = "marker_" + UUID.randomUUID().toString().take(8)
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(options.position.latitude, options.position.longitude))
                .title(options.title)
                .snippet(options.snippet)
                .alpha(options.alpha)
                .rotation(options.rotation)
                .draggable(options.draggable)
                .flat(options.flat)
                .zIndex(options.zIndex)
                .visible(options.visible)
                .anchor(options.anchor.first, options.anchor.second)
                .apply {
                    when (val icon = options.icon) {
                        is IconProvider.FromBitmap -> {
                            val bitmap = icon.bitmapDescriptor as? Bitmap
                                ?: throw IllegalArgumentException("FromBitmap expects android.graphics.Bitmap")
                            this.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        }

                        is IconProvider.FromDrawable -> {
                            // Fallback: use default; drawable loading requires Context
                            Log.d(
                                TAG,
                                "FromDrawable icon requires Context; use FromBitmap for custom icons"
                            )
                        }

                        is IconProvider.FromAsset -> {
                            Log.d(
                                TAG,
                                "FromAsset icon requires Context; use FromBitmap for custom icons"
                            )
                        }

                        is IconProvider.Default,
                        null -> { /* use default marker icon */
                        }
                    }
                }
        ) ?: throw IllegalStateException("Failed to add marker")

        marker.tag = id
        markers[id] = marker
        return GoogleMarker(id, marker)
    }

    override fun removeMarker(marker: ModelMarker) {
        val gMarker = (marker as? GoogleMarker)?.native ?: return
        gMarker.remove()
        markers.remove(marker.id)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.remove()
        markers.remove(id)
    }

    override fun clearMarkers() {
        map.clear()
        markers.clear()
    }

    // ── Shapes ──

    override fun addPolyline(options: ModelPolylineOptions): ModelPolyline {
        val polyline = map.addPolyline(
            PolylineOptions()
                .addAll(options.points.map { LatLng(it.latitude, it.longitude) })
                .color(options.color)
                .width(options.width)
                .zIndex(options.zIndex)
                .clickable(options.clickable)
                .visible(options.visible)
        )
        polylines.add(polyline)
        return GooglePolyline(polyline)
    }

    override fun addPolygon(options: ModelPolygonOptions): ModelPolygon {
        val polygon = map.addPolygon(
            PolygonOptions()
                .addAll(options.points.map { LatLng(it.latitude, it.longitude) })
                .fillColor(options.fillColor)
                .strokeColor(options.strokeColor)
                .strokeWidth(options.strokeWidth)
                .zIndex(options.zIndex)
                .clickable(options.clickable)
                .visible(options.visible)
        )
        polygons.add(polygon)
        return GooglePolygon(polygon)
    }

    override fun addCircle(options: ModelCircleOptions): ModelCircle {
        val circle = map.addCircle(
            CircleOptions()
                .center(LatLng(options.center.latitude, options.center.longitude))
                .radius(options.radius)
                .fillColor(options.fillColor)
                .strokeColor(options.strokeColor)
                .strokeWidth(options.strokeWidth)
                .zIndex(options.zIndex)
                .clickable(options.clickable)
                .visible(options.visible)
        )
        circles.add(circle)
        return GoogleCircle(circle)
    }

    override fun clearShapes() {
        polylines.forEach { it.remove() }
        polylines.clear()
        polygons.forEach { it.remove() }
        polygons.clear()
        circles.forEach { it.remove() }
        circles.clear()
    }

    // ── Map type ──

    override var mapType: MapType
        get() = googleMap?.let { m ->
            when (m.mapType) {
                GoogleMap.MAP_TYPE_NONE -> MapType.NONE
                GoogleMap.MAP_TYPE_NORMAL -> MapType.NORMAL
                GoogleMap.MAP_TYPE_SATELLITE -> MapType.SATELLITE
                GoogleMap.MAP_TYPE_HYBRID -> MapType.HYBRID
                GoogleMap.MAP_TYPE_TERRAIN -> MapType.TERRAIN
                else -> MapType.NORMAL
            }
        } ?: MapType.NORMAL
        set(value) {
            googleMap?.let { applyMapType(value) }
        }

    // ── User location ──

    override fun enableUserLocation(enabled: Boolean) {
        try {
            map.isMyLocationEnabled = enabled
            map.uiSettings.isMyLocationButtonEnabled = false // Use our own button
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission not granted", e)
        }
    }

    // ── Events ──

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            map.setOnMapClickListener { latLng ->
                value?.invoke(ModelLatLng(latLng.latitude, latLng.longitude))
            }
        }

    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            map.setOnMapLongClickListener { latLng ->
                value?.invoke(ModelLatLng(latLng.latitude, latLng.longitude))
            }
        }

    override var onMarkerClick: ((markerId: String) -> Boolean)? = null
        set(value) {
            field = value
            map.setOnMarkerClickListener { marker ->
                val id = marker.tag as? String ?: return@setOnMarkerClickListener false
                value?.invoke(id) ?: false
            }
        }

    override var onCameraChange: ((CameraState) -> Unit)? = null
        set(value) {
            field = value
            map.setOnCameraMoveStartedListener { reason ->
                val r = when (reason) {
                    GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> CameraMoveReason.GESTURE
                    GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> CameraMoveReason.API_ANIMATION
                    GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> CameraMoveReason.API_ANIMATION
                    else -> CameraMoveReason.API_DIRECT
                }
                value?.invoke(CameraState.Started(r))
            }
            map.setOnCameraMoveListener {
                value?.invoke(CameraState.Moving(camera.current))
            }
            map.setOnCameraIdleListener {
                value?.invoke(CameraState.Idle(camera.current))
            }
        }

    // ── Cluster items (non-clustering fallback — throws) ──

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException(
            "Clustering is not supported directly by GoogleMapInstance. " +
                    "Use GoogleClusterableMap for clustering support."
        )
    }

    // ── Cleanup ──

    override fun clearAll() {
        clearMarkers()
        clearShapes()
    }

    companion object {
        private const val TAG = "GoogleMapInstance"
    }
}

// ── Internal wrapper classes ──

private class GoogleMarker(
    override val id: String,
    val native: Marker
) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.position.latitude, native.position.longitude)
        set(value) {
            native.position = LatLng(value.latitude, value.longitude)
        }

    override var visible: Boolean
        get() = native.isVisible
        set(value) {
            native.isVisible = value
        }

    override fun remove() {
        native.remove()
    }
}

private class GooglePolyline(
    private val native: Polyline
) : ModelPolyline {
    override fun remove() {
        native.remove()
    }
}

private class GooglePolygon(
    private val native: Polygon
) : ModelPolygon {
    override fun remove() {
        native.remove()
    }
}

private class GoogleCircle(
    private val native: Circle
) : ModelCircle {
    override fun remove() {
        native.remove()
    }
}
