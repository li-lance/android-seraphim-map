package com.seraphim.core.map.tmap

import android.content.Context
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.skt.tmap.TMapView
import kotlinx.coroutines.CompletableDeferred

class TmapMapHost(private val mapView: TMapView) : MapHost {
    private val ready = CompletableDeferred<TMapView>()

    init {
        mapView.setOnMapReadyListener { ready.complete(mapView) }
    }

    override suspend fun awaitNativeMap(): Any = ready.await()

    override fun updatePadding(left: Int, top: Int, right: Int, bottom: Int) {
        mapView.setPadding(left, top, right, bottom)
    }

    override fun onStart() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onStop() {}
    override fun onDestroy() {}
    override fun onLowMemory() {}

    companion object {
        fun create(context: Context, parent: ViewGroup, apiKey: String = ""): TmapMapHost {
            val mv = TMapView(context).apply { setSKTMapApiKey(apiKey) }
            parent.addView(mv)
            return TmapMapHost(mv)
        }
    }
}
