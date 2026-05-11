package com.seraphim.core.map.google

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.seraphim.core.map.commons.UiSettings

/**
 * Applies [UiSettings] to a Google Map.
 *
 * Unsupported properties are silently ignored with a debug log.
 */
object GoogleMapUiSettings {

    private const val TAG = "GoogleMapUiSettings"

    fun apply(map: GoogleMap, settings: UiSettings) {
        map.uiSettings.apply {
            isScrollGesturesEnabled = settings.scrollGesturesEnabled
            isZoomGesturesEnabled = settings.zoomGesturesEnabled
            isRotateGesturesEnabled = settings.rotateGesturesEnabled
            isTiltGesturesEnabled = settings.tiltGesturesEnabled
            isCompassEnabled = settings.compassEnabled
            isMyLocationButtonEnabled = settings.myLocationButtonEnabled
            isZoomControlsEnabled = settings.zoomControlsEnabled
            isMapToolbarEnabled = settings.mapToolbarEnabled
        }
        map.isTrafficEnabled = settings.trafficEnabled
        try {
            map.isIndoorEnabled = settings.indoorEnabled
        } catch (e: Exception) {
            Log.d(TAG, "setIndoorEnabled not supported", e)
        }
        map.isBuildingsEnabled = settings.buildingsEnabled
    }
}
