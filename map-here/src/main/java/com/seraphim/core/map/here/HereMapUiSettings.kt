package com.seraphim.core.map.here

import android.util.Log
import com.here.sdk.mapview.MapScene
import com.seraphim.core.map.commons.MapUiSettings

class HereMapUiSettings(private val scene: () -> MapScene?) : MapUiSettings {
    private val s: MapScene get() = scene() ?: throw IllegalStateException()

    override var scrollGesturesEnabled: Boolean = true;
        set(v) {
            Log.d(TAG, "scroll: $v")
        }
    override var zoomGesturesEnabled: Boolean = true;
        set(v) {
            Log.d(TAG, "zoom: $v")
        }
    override var rotateGesturesEnabled: Boolean = true;
        set(v) {
            Log.d(TAG, "rotate: $v")
        }
    override var tiltGesturesEnabled: Boolean = true;
        set(v) {
            Log.d(TAG, "tilt: $v")
        }
    override var compassEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "compass: not supported")
        }
    override var myLocationButtonEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "locationBtn: not supported")
        }
    override var zoomControlsEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "zoomCtrls: not supported")
        }
    override var mapToolbarEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "toolbar: not supported")
        }
    override var trafficEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "traffic: $v")
        }
    override var indoorEnabled: Boolean = false;
        set(v) {
            Log.d(TAG, "indoor: not supported")
        }
    override var buildingsEnabled: Boolean = true;
        set(v) {
            Log.d(TAG, "buildings: $v")
        }

    companion object {
        private const val TAG = "HereUiSettings"
    }
}
