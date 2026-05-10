package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.Circle
import com.seraphim.core.map.commons.model.CircleOptions
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.MapType
import com.seraphim.core.map.commons.model.Marker
import com.seraphim.core.map.commons.model.MarkerOptions
import com.seraphim.core.map.commons.model.Polygon
import com.seraphim.core.map.commons.model.PolygonOptions
import com.seraphim.core.map.commons.model.Polyline
import com.seraphim.core.map.commons.model.PolylineOptions

/**
 * Core map instance interface.
 *
 * Provides a unified API for interacting with a map, abstracting away
 * provider-specific details (Google Maps, HERE, Yandex, Tmap).
 *
 * Lifecycle: Call [init] to start, then use the map. Call the [MapHost]'s
 * lifecycle methods as appropriate for your Android lifecycle.
 */
interface MapInstance {
    // ── Sub-components ──

    /** The camera controller. */
    val camera: MapCamera

    /** The UI settings controller. */
    val uiSettings: MapUiSettings

    // ── Initialization ──

    /**
     * Initialize the map with the given host and options.
     * Must be called exactly once.
     */
    suspend fun init(host: MapHost, options: MapOptions)

    /**
     * Refresh the map host reference, e.g., after Fragment recreation.
     */
    suspend fun refreshHost(host: MapHost)

    // ── Markers ──

    /** Add a marker to the map. */
    fun addMarker(options: MarkerOptions): Marker

    /** Remove a specific marker from the map. */
    fun removeMarker(marker: Marker)

    /** Remove a marker by its id. */
    fun removeMarkerById(id: String)

    /** Remove all markers from the map. */
    fun clearMarkers()

    // ── Shapes ──

    /** Add a polyline to the map. */
    fun addPolyline(options: PolylineOptions): Polyline

    /** Add a polygon to the map. */
    fun addPolygon(options: PolygonOptions): Polygon

    /** Add a circle to the map. */
    fun addCircle(options: CircleOptions): Circle

    /** Remove all shapes (polylines, polygons, circles) from the map. */
    fun clearShapes()

    // ── Map type ──

    /** The current map type. */
    var mapType: MapType

    // ── User location ──

    /** Enable or disable the user location indicator on the map. */
    fun enableUserLocation(enabled: Boolean)

    // ── Events ──

    /** Called when the user taps on the map. */
    var onMapClick: ((LatLng) -> Unit)?

    /** Called when the user long-presses on the map. */
    var onMapLongClick: ((LatLng) -> Unit)?

    /**
     * Called when a marker is clicked.
     * @return true if the event was consumed, false to let the provider handle it.
     */
    var onMarkerClick: ((markerId: String) -> Boolean)?

    /** Called when the camera state changes. */
    var onCameraChange: ((CameraState) -> Unit)?

    // ── Cluster items (non-clustering fallback) ──

    /**
     * Set cluster items on the map.
     * Providers without clustering support should throw [UnsupportedOperationException].
     * Use [ClusterableMap] for full clustering support.
     */
    @Throws(UnsupportedOperationException::class)
    fun setClusterItems(items: List<ClusterItem>)

    // ── Cleanup ──

    /** Clear all map content (markers, shapes, overlays). */
    fun clearAll()
}
