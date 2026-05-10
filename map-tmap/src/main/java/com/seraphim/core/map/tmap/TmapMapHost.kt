package com.seraphim.core.map.tmap

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.skt.Tmap.TMapView
import kotlinx.coroutines.CompletableDeferred

/**
 * [MapHost] implementation wrapping a Tmap [TMapView].
 */
class TmapMapHost(
    private val mapView: TMapView
) : MapHost {

    private val mapReady = CompletableDeferred<TMapView>()

    init {
        mapReady.complete(mapView)
    }

    override suspend fun awaitNativeMap(): Any = mapReady.await()

    override fun updatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(TAG, "updatePadding: $left, $top, $right, $bottom")
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        // TMapView cleanup handled by caller
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
    }

    companion object {
        private const val TAG = "TmapMapHost"
        fun create(context: Context, parent: ViewGroup): TmapMapHost {
            val mapView = TMapView(context)
            parent.addView(mapView)
            return TmapMapHost(mapView)
        }
    }
}
