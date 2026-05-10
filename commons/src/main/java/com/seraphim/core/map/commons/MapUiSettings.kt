package com.seraphim.core.map.commons

/**
 * Runtime UI settings for a map instance.
 * Setting an unsupported property is silently ignored by the provider.
 */
interface MapUiSettings {
    // Gestures
    var scrollGesturesEnabled: Boolean
    var zoomGesturesEnabled: Boolean
    var rotateGesturesEnabled: Boolean
    var tiltGesturesEnabled: Boolean

    // Controls
    var compassEnabled: Boolean
    var myLocationButtonEnabled: Boolean
    var zoomControlsEnabled: Boolean
    var mapToolbarEnabled: Boolean

    // Map content
    var trafficEnabled: Boolean
    var indoorEnabled: Boolean
    var buildingsEnabled: Boolean
}
