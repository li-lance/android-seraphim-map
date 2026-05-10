package com.seraphim.core.map.tmap

import com.seraphim.core.map.commons.ClusterableMap
import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem
import com.skt.tmap.TMapPoint
import com.skt.tmap.overlay.TMapMarkerCluster
import com.skt.tmap.overlay.TMapMarkerItem

class TmapClusterableMap : TmapMapInstance(), ClusterableMap {

    private var cluster: TMapMarkerCluster? = null

    override suspend fun init(
        host: com.seraphim.core.map.commons.MapHost,
        options: com.seraphim.core.map.commons.MapOptions
    ) {
        super.init(host, options)
        val mv = host.awaitNativeMap() as? com.skt.tmap.TMapView
            ?: throw IllegalArgumentException("MapHost must provide a TMapView")
        cluster = TMapMarkerCluster(mv)
    }

    override fun setClusterItems(items: List<ClusterItem>) {
        val c = cluster ?: throw IllegalStateException("Not initialized")
        val markerList = items.map { item ->
            TMapMarkerItem().apply {
                id = item.id
                tMapPoint = TMapPoint(item.position.latitude, item.position.longitude)
                canShowCallout = true
                calloutTitle = item.title
                calloutSubTitle = item.snippet
            }
        }
        c.setMarkers(markerList)
        c.updateClusters()
        onClusterFinish?.invoke()
    }

    override fun clearClusterItems() {
        cluster?.setMarkers(emptyList())
    }

    override var onClusterClick: ((ClusterInfo) -> Boolean)? = null
    override var onClusterItemClick: ((ClusterItem) -> Boolean)? = null
    override var onClusterFinish: (() -> Unit)? = null

    override fun clearAll() {
        clearClusterItems()
        super.clearAll()
    }
}
