package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLngBounds
import com.seraphim.core.map.commons.model.MapPadding
import com.seraphim.core.map.commons.model.MapType

/**
 * Camera, map type, and padding controls.
 *
 * Provider implementations typically delegate these methods to their
 * [MapCamera] instance, which already handles position/bounds/padding.
 */
interface MapViewport {
    /** Current map type. */
    var mapType: MapType

    /** Move the camera to a specific position. */
    fun moveCamera(position: CameraPosition, animate: Boolean = true)

    /** Animate the camera to show the given bounds with padding. */
    fun animateCameraToBounds(bounds: LatLngBounds, padding: Int = 0)

    /** Current camera position. */
    val cameraPosition: CameraPosition

    /** Set padding around the map edges. */
    fun setPadding(padding: MapPadding)

    /** Reset padding to zero. */
    fun resetPadding()
}
