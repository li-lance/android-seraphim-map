package com.seraphim.core.map.tmap

import android.util.Log
import com.seraphim.core.map.commons.MapUiSettings
import com.skt.tmap.TMapView

class TmapMapUiSettings(private val mv: () -> TMapView?) : MapUiSettings {
    private val m: TMapView get() = mv() ?: throw IllegalStateException()

    override var scrollGesturesEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "scroll: not directly supported")
        }
    override var zoomGesturesEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "zoom: not directly supported")
        }
    override var rotateGesturesEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "rotate: not directly supported")
        }
    override var tiltGesturesEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "tilt: not supported")
        }
    override var compassEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "compass: not supported")
        }
    override var myLocationButtonEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "mylocation: not supported")
        }
    override var zoomControlsEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "zoomControls: not supported")
        }
    override var mapToolbarEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "mapToolbar: not supported")
        }
    override var trafficEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "traffic: use TMapTrafficLine")
        }
    override var indoorEnabled: Boolean = false
        set(v) {
            Log.d(TAG, "indoor: not supported")
        }
    override var buildingsEnabled: Boolean = true
        set(v) {
            Log.d(TAG, "buildings: not supported")
        }

    companion object {
        private const val TAG = "TmapMapUiSettings"
    }
}
