package com.seraphim.core.map.tmap

import android.util.Log
import com.seraphim.core.map.commons.MapUiSettings
import com.skt.Tmap.TMapView

class TmapMapUiSettings(
    private val mapViewProvider: () -> TMapView?
) : MapUiSettings {

    private val mapView: TMapView
        get() = mapViewProvider() ?: throw IllegalStateException("TMapView not available")

    override var scrollGesturesEnabled: Boolean = true
        set(value) {
            Log.d(TAG, "scrollGesturesEnabled: not directly supported by Tmap SDK")
        }
    override var zoomGesturesEnabled: Boolean = true
        set(value) {
            Log.d(TAG, "zoomGesturesEnabled: not directly supported by Tmap SDK")
        }
    override var rotateGesturesEnabled: Boolean = true
        set(value) {
            Log.d(TAG, "rotateGesturesEnabled: not directly supported by Tmap SDK")
        }
    override var tiltGesturesEnabled: Boolean = true
        set(value) {
            Log.d(TAG, "tiltGesturesEnabled: not supported by Tmap SDK")
        }
    override var compassEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "compassEnabled: not supported by Tmap SDK")
        }
    override var myLocationButtonEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "myLocationButtonEnabled: not supported by Tmap SDK")
        }
    override var zoomControlsEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "zoomControlsEnabled: not supported by Tmap SDK")
        }
    override var mapToolbarEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "mapToolbarEnabled: not supported by Tmap SDK")
        }
    override var trafficEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "trafficEnabled: use TMapTapi for traffic")
        }
    override var indoorEnabled: Boolean = false
        set(value) {
            Log.d(TAG, "indoorEnabled: not supported by Tmap SDK")
        }
    override var buildingsEnabled: Boolean = true
        set(value) {
            Log.d(TAG, "buildingsEnabled: not directly supported by Tmap SDK")
        }

    companion object {
        private const val TAG = "TmapMapUiSettings"
    }
}
