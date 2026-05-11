package com.seraphim.core.map.amap

import android.util.Log
import com.amap.api.maps.AMap
import com.seraphim.core.map.commons.MapUiSettings

class AMapMapUiSettings(private val am: () -> AMap?) : MapUiSettings {
    private val m: AMap get() = am() ?: throw IllegalStateException()
    private val ui get() = m.uiSettings

    override var scrollGesturesEnabled: Boolean
        get() = ui.isScrollGesturesEnabled
        set(v) {
            ui.isScrollGesturesEnabled = v
        }
    override var zoomGesturesEnabled: Boolean
        get() = ui.isZoomGesturesEnabled
        set(v) {
            ui.isZoomGesturesEnabled = v
        }
    override var rotateGesturesEnabled: Boolean
        get() = ui.isRotateGesturesEnabled
        set(v) {
            ui.isRotateGesturesEnabled = v
        }
    override var tiltGesturesEnabled: Boolean
        get() = ui.isTiltGesturesEnabled
        set(v) {
            ui.isTiltGesturesEnabled = v
        }
    override var compassEnabled: Boolean
        get() = ui.isCompassEnabled
        set(v) {
            ui.isCompassEnabled = v
        }
    override var myLocationButtonEnabled: Boolean
        get() = ui.isMyLocationButtonEnabled
        set(v) {
            ui.isMyLocationButtonEnabled = v
        }
    override var zoomControlsEnabled: Boolean
        get() = ui.isZoomControlsEnabled
        set(v) {
            ui.isZoomControlsEnabled = v
        }
    override var mapToolbarEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "mapToolbar: not supported")
        }
    override var trafficEnabled: Boolean
        get() = m.isTrafficEnabled
        set(v) {
            m.isTrafficEnabled = v
        }
    override var indoorEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "indoor: not supported")
        }
    override var buildingsEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "buildings: not directly supported")
        }

    companion object {
        private const val TAG = "AMapUiSettings"
    }
}
