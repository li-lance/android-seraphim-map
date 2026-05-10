package com.seraphim.core.map.here

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapHost
import kotlinx.coroutines.CompletableDeferred

/**
 * [MapHost] implementation wrapping a HERE SDK [MapView].
 *
 * Manages the lifecycle and provides access to the [MapView]
 * (which gives access to [com.here.sdk.mapview.MapScene] via [MapView.mapScene]).
 */
class HereMapHost(
    private val mapView: MapView
) : MapHost {

    private val mapReady = CompletableDeferred<MapView>()

    init {
        mapView.onCreate(null)
        mapView.onResume()
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { errorCode ->
            if (errorCode == null) {
                mapReady.complete(mapView)
            } else {
                Log.e(TAG, "Failed to load map scene: $errorCode")
                mapReady.completeExceptionally(
                    RuntimeException("Failed to load HERE map scene: $errorCode")
                )
            }
        }
    }

    override suspend fun awaitNativeMap(): Any = mapReady.await()

    override fun updatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(TAG, "updatePadding: $left, $top, $right, $bottom")
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
    }

    companion object {
        private const val TAG = "HereMapHost"

        fun create(context: Context, parent: ViewGroup): HereMapHost {
            val mapView = MapView(context)
            parent.addView(mapView)
            return HereMapHost(mapView)
        }
    }
}
