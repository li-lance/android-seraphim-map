package com.seraphim.core.map.commons

/**
 * Configuration for map UI controls and gestures.
 * Unsupported features are silently ignored by provider implementations (logged at debug level).
 */
data class UiSettings(
    // Gestures
    val scrollGesturesEnabled: Boolean = true,
    val zoomGesturesEnabled: Boolean = true,
    val rotateGesturesEnabled: Boolean = true,
    val tiltGesturesEnabled: Boolean = true,

    // Controls
    val compassEnabled: Boolean = false,
    val myLocationButtonEnabled: Boolean = false,
    val zoomControlsEnabled: Boolean = false,
    val mapToolbarEnabled: Boolean = false,

    // Map content
    val trafficEnabled: Boolean = false,
    val indoorEnabled: Boolean = false,
    val buildingsEnabled: Boolean = true
)
