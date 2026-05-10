package com.seraphim.core.map.google

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.seraphim.core.map.commons.ClusterableMap
import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng

/**
 * [ClusterableMap] implementation for Google Maps using [ClusterManager].
 *
 * Requires Google Maps Utils library (android-maps-utils).
 */
class GoogleClusterableMap(
    private val context: Context
) : GoogleMapInstance(), ClusterableMap {

    private var clusterManager: ClusterManager<MapClusterItem>? = null

    override suspend fun init(
        host: com.seraphim.core.map.commons.MapHost,
        options: com.seraphim.core.map.commons.MapOptions
    ) {
        super.init(host, options)
        val map = host.awaitNativeMap() as? GoogleMap
            ?: throw IllegalArgumentException("MapHost must provide a GoogleMap instance")

        val cm = ClusterManager<MapClusterItem>(context, map)
        val renderer: DefaultClusterRenderer<MapClusterItem> =
            DefaultClusterRenderer(context, map, cm).apply {
                setAnimation(false)
            }
        cm.renderer = renderer

        cm.setOnClusterClickListener { cluster: Cluster<MapClusterItem> ->
            val items = cluster.items.map { it.toModel() }
            onClusterClick?.invoke(
                ClusterInfo(
                    items = items,
                    position = ModelLatLng(cluster.position.latitude, cluster.position.longitude),
                    size = cluster.size
                )
            ) ?: false
        }

        cm.setOnClusterItemClickListener { item: MapClusterItem ->
            onClusterItemClick?.invoke(item.toModel()) ?: false
        }

        clusterManager = cm
        map.setOnCameraIdleListener {
            clusterManager?.onCameraIdle()
            onClusterFinish?.invoke()
        }
    }

    override fun setClusterItems(items: List<ClusterItem>) {
        val manager = clusterManager
            ?: throw IllegalStateException("Not initialized. Call init() first.")
        manager.clearItems()
        manager.addItems(items.map { MapClusterItem(it) })
        manager.cluster()
    }

    override fun clearClusterItems() {
        clusterManager?.clearItems()
    }

    override var onClusterClick: ((ClusterInfo) -> Boolean)? = null
    override var onClusterItemClick: ((ClusterItem) -> Boolean)? = null
    override var onClusterFinish: (() -> Unit)? = null

    override fun clearAll() {
        clearClusterItems()
        super.clearAll()
    }

    companion object {
        private const val TAG = "GoogleClusterableMap"
    }
}

/**
 * Internal adapter from [ClusterItem] to [com.google.maps.android.clustering.ClusterItem].
 */
private class MapClusterItem(
    private val data: ClusterItem
) : com.google.maps.android.clustering.ClusterItem {
    override fun getPosition(): LatLng = LatLng(data.position.latitude, data.position.longitude)
    override fun getTitle(): String = data.title
    override fun getSnippet(): String = data.snippet
    override fun getZIndex(): Float = data.zIndex

    fun toModel(): ClusterItem = data
}
