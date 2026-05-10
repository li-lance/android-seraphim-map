package com.seraphim.core.map.google

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.seraphim.core.map.commons.MapUiSettings

/**
 * [MapUiSettings] implementation for Google Maps.
 *
 * Unsupported properties are silently ignored with a debug log.
 */
class GoogleMapUiSettings(
    private val mapProvider: () -> GoogleMap?
) : MapUiSettings {

    private val map: GoogleMap
        get() = mapProvider()
            ?: throw IllegalStateException("GoogleMap is not available. Has init() been called?")

    override var scrollGesturesEnabled: Boolean
        get() = map.uiSettings.isScrollGesturesEnabled
        set(value) {
            map.uiSettings.isScrollGesturesEnabled = value
        }

    override var zoomGesturesEnabled: Boolean
        get() = map.uiSettings.isZoomGesturesEnabled
        set(value) {
            map.uiSettings.isZoomGesturesEnabled = value
        }

    override var rotateGesturesEnabled: Boolean
        get() = map.uiSettings.isRotateGesturesEnabled
        set(value) {
            map.uiSettings.isRotateGesturesEnabled = value
        }

    override var tiltGesturesEnabled: Boolean
        get() = map.uiSettings.isTiltGesturesEnabled
        set(value) {
            map.uiSettings.isTiltGesturesEnabled = value
        }

    override var compassEnabled: Boolean
        get() = map.uiSettings.isCompassEnabled
        set(value) {
            map.uiSettings.isCompassEnabled = value
        }

    override var myLocationButtonEnabled: Boolean
        get() = map.uiSettings.isMyLocationButtonEnabled
        set(value) {
            map.uiSettings.isMyLocationButtonEnabled = value
        }

    override var zoomControlsEnabled: Boolean
        get() = map.uiSettings.isZoomControlsEnabled
        set(value) {
            map.uiSettings.isZoomControlsEnabled = value
        }

    override var mapToolbarEnabled: Boolean
        get() = map.uiSettings.isMapToolbarEnabled
        set(value) {
            map.uiSettings.isMapToolbarEnabled = value
        }

    override var trafficEnabled: Boolean
        get() = map.isTrafficEnabled
        set(value) {
            map.isTrafficEnabled = value
        }

    override var indoorEnabled: Boolean
        get() = map.isIndoorEnabled
        set(value) {
            try {
                map.isIndoorEnabled = value
            } catch (e: Exception) {
                Log.d(TAG, "setIndoorEnabled not supported", e)
            }
        }

    override var buildingsEnabled: Boolean
        get() = map.isBuildingsEnabled
        set(value) {
            map.isBuildingsEnabled = value
        }

    companion object {
        private const val TAG = "GoogleMapUiSettings"
    }
}
