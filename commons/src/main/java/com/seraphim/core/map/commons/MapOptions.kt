package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.seraphim.core.map.commons.model.MapPadding
import com.seraphim.core.map.commons.model.MapType

/**
 * Initialization options for a map instance.
 */
data class MapOptions(
    val style: MapStyle = MapStyle.Default,
    val interactive: Boolean = true,
    val padding: MapPadding = MapPadding.NONE,
    val initialCamera: InitialCamera = InitialCamera.None,
    val mapType: MapType = MapType.NORMAL,
    val uiSettings: UiSettings = UiSettings(),
    val credentials: MapCredentials = MapCredentials.None
)

/**
 * Initial camera position for the map.
 */
sealed class InitialCamera {
    /** Let the provider use its default position. */
    object None : InitialCamera()

    /** Position the camera at a specific location and zoom level. */
    data class Position(val target: LatLng, val zoom: Float = 15f) : InitialCamera()

    /** Fit the camera to display the given bounds. */
    data class Bounds(val bounds: LatLngBounds, val padding: Int = 0) : InitialCamera()
}
