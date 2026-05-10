package com.seraphim.core.map.here

import android.util.Log
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapUiSettings

/**
 * [MapUiSettings] implementation for HERE SDK.
 *
 * HERE SDK has limited UI control compared to Google Maps.
 * Unsupported properties are silently ignored with a debug log.
 */
class HereMapUiSettings(
    private val mapViewProvider: () -> MapView?
) : MapUiSettings {

    private val mapView: MapView
        get() = mapViewProvider()
            ?: throw IllegalStateException("MapView is not available. Has init() been called?")

    override var scrollGesturesEnabled: Boolean
        get() = mapView.gestures.isPanEnabled
        set(value) {
            mapView.gestures.isPanEnabled = value
        }

    override var zoomGesturesEnabled: Boolean
        get() = mapView.gestures.isPinchRotateEnabled
        set(value) {
            mapView.gestures.isPinchRotateEnabled = value
        }

    override var rotateGesturesEnabled: Boolean
        get() = mapView.gestures.isTwoFingerPanEnabled
        set(value) {
            mapView.gestures.isTwoFingerPanEnabled = value
        }

    override var tiltGesturesEnabled: Boolean
        get() = true
        set(value) {
            Log.d(TAG, "tiltGesturesEnabled: HERE SDK does not have separate tilt gesture control")
        }

    override var compassEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "compassEnabled: not directly supported by HERE SDK")
        }

    override var myLocationButtonEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "myLocationButtonEnabled: not directly supported by HERE SDK")
        }

    override var zoomControlsEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "zoomControlsEnabled: not supported by HERE SDK")
        }

    override var mapToolbarEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "mapToolbarEnabled: not supported by HERE SDK")
        }

    override var trafficEnabled: Boolean
        get() = mapView.mapScene.getLayerState(
            com.here.sdk.mapview.MapScene.Layers.TRAFFIC_FLOW
        ) == com.here.sdk.mapview.MapScene.LayerState.VISIBLE
        set(value) {
            mapView.mapScene.setLayerState(
                com.here.sdk.mapview.MapScene.Layers.TRAFFIC_FLOW,
                if (value) com.here.sdk.mapview.MapScene.LayerState.VISIBLE
                else com.here.sdk.mapview.MapScene.LayerState.HIDDEN
            )
        }

    override var indoorEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "indoorEnabled: not supported by HERE SDK")
        }

    override var buildingsEnabled: Boolean
        get() = true
        set(value) {
            Log.d(TAG, "buildingsEnabled: HERE SDK always shows buildings in 3D mode")
        }

    companion object {
        private const val TAG = "HereMapUiSettings"
    }
}
