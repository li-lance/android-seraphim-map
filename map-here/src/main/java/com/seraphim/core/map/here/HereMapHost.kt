package com.seraphim.core.map.here

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapHost
import kotlinx.coroutines.CompletableDeferred

class HereMapHost(private val mapView: MapView) : MapHost {
    private var scene: com.here.sdk.mapview.MapScene? = null
    private val ready = CompletableDeferred<MapView>()

    init {
        mapView.onCreate(null)
        // Use loadScene callback instead of onReadyListener (per HERE 4.25.5 pattern)
        loadMapScene()
    }

    private fun loadMapScene() {
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { error ->
            if (error == null) {
                scene = mapView.mapScene
                ready.complete(mapView)
            }
        }
    }

    override suspend fun awaitNativeMap(): Any = ready.await()
    override fun updatePadding(l: Int, t: Int, r: Int, b: Int) {}

    override fun onStart() {}
    override fun onResume() {
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
    }
    override fun onStop() {}
    override fun onDestroy() {
        mapView.onDestroy(); scene = null
    }
    override fun onLowMemory() {}

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        private var sdkInitialized = false

        @Synchronized
        fun initSDK(context: Context, accessKeyId: String, accessKeySecret: String) {
            if (sdkInitialized) return
            val auth = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            try {
                SDKNativeEngine.makeSharedInstance(context, SDKOptions(auth))
                sdkInitialized = true
            } catch (e: InstantiationErrorException) {
                throw RuntimeException("HERE SDK init failed: ${e.error.name}", e)
            }
        }

        fun create(ctx: Context, parent: ViewGroup): HereMapHost {
            val mv = MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            parent.addView(mv)
            return HereMapHost(mv)
        }
    }
}
