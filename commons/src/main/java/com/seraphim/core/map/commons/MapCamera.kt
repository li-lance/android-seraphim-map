package com.seraphim.core.map.commons

import android.graphics.Point
import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds

/**
 * Controls the map camera (position, zoom, tilt, bearing).
 */
interface MapCamera {
    /** The current camera position. */
    val current: CameraPosition

    /** The currently visible region of the map. */
    val visibleRegion: LatLngBounds

    /**
     * Move the camera instantly (no animation).
     * @param target The target location.
     * @param zoom If non-null, set the zoom level.
     */
    fun moveTo(target: LatLng, zoom: Float? = null)

    /**
     * Animate the camera to a new position.
     * @param target The target location.
     * @param zoom If non-null, set the zoom level.
     * @param tilt If non-null, set the tilt angle.
     * @param bearing If non-null, set the bearing.
     * @param durationMs Animation duration in milliseconds.
     */
    fun animateTo(
        target: LatLng,
        zoom: Float? = null,
        tilt: Float? = null,
        bearing: Float? = null,
        durationMs: Int = 300
    )

    /**
     * Animate the camera to fit the given bounds.
     * @param bounds The bounds to fit.
     * @param paddingPx Additional padding in pixels.
     * @param durationMs Animation duration in milliseconds.
     * @param onFinish Called when animation completes. canceled=true if interrupted.
     */
    fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int = 0,
        durationMs: Int = 300,
        onFinish: ((canceled: Boolean) -> Unit)? = null
    )

    /** Zoom in by one level. */
    fun zoomIn()

    /** Zoom out by one level. */
    fun zoomOut()

    /** Zoom by a relative amount (positive = in, negative = out). */
    fun zoomBy(amount: Float)

    /** Convert a screen point to a geographical location. */
    fun screenToLatLng(x: Int, y: Int): LatLng

    /** Convert a geographical location to a screen point. */
    fun latLngToScreen(location: LatLng): Point
}
