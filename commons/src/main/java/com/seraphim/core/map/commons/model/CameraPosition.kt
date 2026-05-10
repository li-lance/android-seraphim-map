package com.seraphim.core.map.commons.model

/**
 * The position of the map camera, including target, zoom, tilt, and bearing.
 */
data class CameraPosition(
    val target: LatLng,
    val zoom: Float = 15f,
    val tilt: Float = 0f,
    val bearing: Float = 0f
) {
    companion object {
        val DEFAULT = CameraPosition(
            target = LatLng(0.0, 0.0),
            zoom = 15f,
            tilt = 0f,
            bearing = 0f
        )
    }
}
