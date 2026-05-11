package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.LatLng

/**
 * Map interaction events.
 */
interface MapEvents {
    /** Called when the map is tapped. */
    var onMapClick: ((LatLng) -> Unit)?

    /** Called when the map is long-pressed. */
    var onMapLongClick: ((LatLng) -> Unit)?

    /** Called when a marker is tapped. Return true to consume the event. */
    var onMarkerClick: ((markerId: String) -> Boolean)?

    /** Called when camera movement state changes (started, moving, idle). */
    var onCameraChange: ((CameraState) -> Unit)?
}
