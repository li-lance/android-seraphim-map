package com.seraphim.core.map.google

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.seraphim.core.map.commons.MapHost
import kotlinx.coroutines.CompletableDeferred

/**
 * [MapHost] implementation wrapping a [SupportMapFragment].
 *
 * Manages the lifecycle of the Google Map and provides access
 * to the [GoogleMap] instance via [awaitNativeMap].
 */
class GoogleMapHost(
    private val mapFragment: SupportMapFragment
) : MapHost, OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val mapReady = CompletableDeferred<GoogleMap>()

    init {
        mapFragment.getMapAsync(this)
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
        Log.d(TAG, "onResume")
        mapFragment.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        mapFragment.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mapFragment.onDestroy()
        googleMap = null
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        mapFragment.onLowMemory()
    }

    companion object {
        private const val TAG = "GoogleMapHost"
    }
}
