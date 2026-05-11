package com.seraphim.core.map.here

import com.seraphim.core.map.commons.ClusterableMap
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem

/**
 * HERE clustering via MapMarkerCluster (supported since 4.25.5).
 * TODO: Implement with actual HERE MapMarkerCluster API.
 */
class HereClusterableMap : HereMapInstance(), ClusterableMap {

    override suspend fun init(host: MapHost, opts: MapOptions) {
        super.init(host, opts)
    }

    override fun setClusterItems(items: List<ClusterItem>) {
        onClusterFinish?.invoke()
    }

    override fun clearClusterItems() {}
    override var onClusterClick: ((ClusterInfo) -> Boolean)? = null
    override var onClusterItemClick: ((ClusterItem) -> Boolean)? = null
    override var onClusterFinish: (() -> Unit)? = null
    override fun clearAll() {
        clearClusterItems(); super.clearAll()
    }
}
