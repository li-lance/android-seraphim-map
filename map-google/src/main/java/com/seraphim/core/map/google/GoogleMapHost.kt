package com.seraphim.core.map.google

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.seraphim.core.map.commons.MapHost
import kotlinx.coroutines.CompletableDeferred

/**
 * [MapHost] implementation wrapping a [SupportMapFragment] or [MapView].
 */
class GoogleMapHost : MapHost, OnMapReadyCallback {

    private var mapFragment: SupportMapFragment? = null
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private val mapReady = CompletableDeferred<GoogleMap>()

    /** Construct with SupportMapFragment (Activity use case). */
    constructor(mapFragment: SupportMapFragment) {
        this.mapFragment = mapFragment
        mapFragment.getMapAsync(this)
    }

    /** Construct with MapView (Fragment/ViewGroup use case). */
    constructor(mapView: MapView) {
        this.mapView = mapView
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapReady.complete(map)
    }

    override suspend fun awaitNativeMap(): Any = mapReady.await()

    override fun updatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        googleMap?.setPadding(left, top, right, bottom)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        mapView?.onResume(); mapFragment?.onResume()
    }

    override fun onPause() {
        mapView?.onPause(); mapFragment?.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }
    override fun onDestroy() {
        mapView?.onDestroy(); mapFragment?.onDestroy()
        googleMap = null
    }

    override fun onLowMemory() {
        mapView?.onLowMemory(); mapFragment?.onLowMemory()
    }

    companion object {
        private const val TAG = "GoogleMapHost"

        fun createWithMapView(context: Context, parent: ViewGroup): GoogleMapHost {
            val mv = MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCreate(null)
            }
            parent.addView(mv)
            return GoogleMapHost(mv)
        }
    }
}
