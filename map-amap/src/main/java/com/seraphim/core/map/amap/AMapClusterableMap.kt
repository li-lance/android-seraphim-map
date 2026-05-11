package com.seraphim.core.map.amap

import com.seraphim.core.map.commons.ClusterableMap
import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem

class AMapClusterableMap : AMapMapInstance(), ClusterableMap {

    override suspend fun init(
        host: com.seraphim.core.map.commons.MapHost,
        opts: com.seraphim.core.map.commons.MapOptions
    ) {
        super.init(host, opts)
        // AMap clustering: use MultiPositionOverlay or ClusterOverlay from AMap SDK
    }

    override fun setClusterItems(items: List<ClusterItem>) {
        // TODO: Implement AMap clustering via ClusterOverlay or custom algorithm
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
