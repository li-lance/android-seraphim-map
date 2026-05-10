package com.seraphim.core.map.yandex

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.CompletableDeferred

/**
 * [MapHost] implementation wrapping a Yandex [MapView].
 */
class YandexMapHost(
    private val mapView: MapView
) : MapHost {

    private val mapReady = CompletableDeferred<MapView>()

    init {
        mapView.onStart()
        mapReady.complete(mapView)
    }

    override suspend fun awaitNativeMap(): Any = mapReady.await()

    override fun updatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(TAG, "updatePadding: $left, $top, $right, $bottom")
    }

    override fun onStart() {
        mapView.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        mapView.onStop()
    }

    override fun onDestroy() {
        mapView.onStop()
        Log.d(TAG, "onDestroy")
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
    }

    companion object {
        private const val TAG = "YandexMapHost"

        fun create(context: Context, parent: ViewGroup): YandexMapHost {
            val mapView = MapView(context)
            parent.addView(mapView)
            return YandexMapHost(mapView)
        }
    }
}
