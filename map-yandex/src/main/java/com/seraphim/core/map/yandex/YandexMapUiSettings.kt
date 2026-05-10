package com.seraphim.core.map.yandex

import android.util.Log
import com.seraphim.core.map.commons.MapUiSettings
import com.yandex.mapkit.mapview.MapView

/**
 * [MapUiSettings] implementation for Yandex MapKit.
 */
class YandexMapUiSettings(
    private val mapViewProvider: () -> MapView?
) : MapUiSettings {

    private val mapView: MapView
        get() = mapViewProvider()
            ?: throw IllegalStateException("MapView not available")

    override var scrollGesturesEnabled: Boolean
        get() = true
        set(value) {
            mapView.mapWindow.map.isScrollGesturesEnabled = value
        }

    override var zoomGesturesEnabled: Boolean
        get() = true
        set(value) {
            mapView.mapWindow.map.isZoomGesturesEnabled = value
        }

    override var rotateGesturesEnabled: Boolean
        get() = true
        set(value) {
            mapView.mapWindow.map.isRotateGesturesEnabled = value
        }

    override var tiltGesturesEnabled: Boolean
        get() = true
        set(value) {
            mapView.mapWindow.map.isTiltGesturesEnabled = value
        }

    override var compassEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "compassEnabled: not directly supported by Yandex MapKit")
        }

    override var myLocationButtonEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "myLocationButtonEnabled: not directly supported by Yandex MapKit")
        }

    override var zoomControlsEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "zoomControlsEnabled: not supported by Yandex MapKit")
        }

    override var mapToolbarEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "mapToolbarEnabled: not supported by Yandex MapKit")
        }

    override var trafficEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "trafficEnabled: use TrafficLayer for Yandex")
        }

    override var indoorEnabled: Boolean
        get() = false
        set(value) {
            Log.d(TAG, "indoorEnabled: not supported by Yandex MapKit")
        }

    override var buildingsEnabled: Boolean
        get() = true
        set(value) {
            Log.d(TAG, "buildingsEnabled: 3D buildings enabled by default in Yandex")
        }

    companion object {
        private const val TAG = "YandexMapUiSettings"
    }
}
