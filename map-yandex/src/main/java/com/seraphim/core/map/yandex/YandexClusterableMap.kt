package com.seraphim.core.map.yandex

import com.seraphim.core.map.commons.ClusterableMap
import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng

/**
 * [ClusterableMap] implementation for Yandex MapKit using [ClusterizedPlacemarkCollection].
 */
class YandexClusterableMap : YandexMapInstance(), ClusterableMap {

    private var clusterCollection: ClusterizedPlacemarkCollection? = null

    override suspend fun init(
        host: com.seraphim.core.map.commons.MapHost,
        options: com.seraphim.core.map.commons.MapOptions
    ) {
        super.init(host, options)
        val mv = host.awaitNativeMap() as? com.yandex.mapkit.mapview.MapView
            ?: throw IllegalArgumentException("MapHost must provide a MapView")

        clusterCollection = mv.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(
            object : ClusterListener {
                override fun onClusterAdded(cluster: com.yandex.mapkit.map.Cluster) {
                    val items = cluster.placemarks.mapNotNull {
                        it.userData as? ClusterItem
                    }
                    val appearance = cluster.appearance
                    onClusterClick?.invoke(
                        ClusterInfo(
                            items = items,
                            position = ModelLatLng(
                                appearance.geometry.latitude,
                                appearance.geometry.longitude
                            ),
                            size = cluster.size
                        )
                    )
                }
            }
        )
    }

    override fun setClusterItems(items: List<ClusterItem>) {
        val collection = clusterCollection
            ?: throw IllegalStateException("Not initialized. Call init() first.")
        collection.clear()
        items.forEach { item ->
            val placemark = collection.addPlacemark(
                Point(item.position.latitude, item.position.longitude)
            )
            placemark.userData = item
        }
        collection.clusterPlacemarks(60.0, 15)
        onClusterFinish?.invoke()
    }

    override fun clearClusterItems() {
        clusterCollection?.clear()
    }

    override var onClusterClick: ((ClusterInfo) -> Boolean)? = null
    override var onClusterItemClick: ((ClusterItem) -> Boolean)? = null
    override var onClusterFinish: (() -> Unit)? = null

    override fun clearAll() {
        clearClusterItems()
        super.clearAll()
    }

    companion object {
        private const val TAG = "YandexClusterableMap"
    }
}
