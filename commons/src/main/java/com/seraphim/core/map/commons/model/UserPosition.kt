package com.seraphim.core.map.commons.model

/**
 * Represents the user's current position from location services.
 */
data class UserPosition(
    /** The geographical location. */
    val location: LatLng,
    /** The bearing (heading) in degrees clockwise from north. */
    val bearing: Float = 0f,
    /** The accuracy radius in meters. */
    val accuracy: Float = 0f,
    /** The timestamp of this location fix. */
    val timestamp: Long = 0L
) {
    companion object {
        val NONE = UserPosition(location = LatLng(0.0, 0.0))
    }
}
