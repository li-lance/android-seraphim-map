package com.seraphim.core.map.amap

import android.content.Context
import android.view.ViewGroup
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.seraphim.core.map.commons.MapHost
import kotlinx.coroutines.CompletableDeferred

class AMapMapHost(private val mapView: MapView) : MapHost {
    private var aMap: AMap? = null
    private val ready = CompletableDeferred<AMap>()

    init {
        mapView.onCreate(null)
        aMap = mapView.map
        aMap?.let { ready.complete(it) }
    }

    override suspend fun awaitNativeMap(): Any = ready.await()
    override fun updatePadding(l: Int, t: Int, r: Int, b: Int) {
        // AMap padding handled via setMapPadding or similar API
    }

    override fun onStart() {}
    override fun onResume() {
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
    }

    override fun onStop() {}
    override fun onDestroy() {
        mapView.onDestroy(); aMap = null
    }

    override fun onLowMemory() {}

    companion object {
        fun create(ctx: Context, parent: ViewGroup): AMapMapHost {
            MapsInitializer.updatePrivacyShow(ctx, true, true)
            MapsInitializer.updatePrivacyAgree(ctx, true)
            val mv = MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            parent.addView(mv)
            return AMapMapHost(mv)
        }
    }
}
