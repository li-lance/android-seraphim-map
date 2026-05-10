package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.ClusterInfo
import com.seraphim.core.map.commons.model.ClusterItem

/**
 * Extension of [MapInstance] that supports clustering.
 *
 * Providers without native clustering support should NOT implement this interface,
 * and instead throw [UnsupportedOperationException] in [MapInstance.setClusterItems].
 *
 * A future generic clustering algorithm module may provide a fallback implementation.
 */
interface ClusterableMap : MapInstance {
    /**
     * Set the cluster items on the map. Clears existing items.
     * Overrides [MapInstance.setClusterItems] with actual clustering implementation.
     */
    override fun setClusterItems(items: List<ClusterItem>)

    /**
     * Remove all cluster items.
     */
    fun clearClusterItems()

    /**
     * Called when a cluster is clicked.
     * @return true if the event was consumed.
     */
    var onClusterClick: ((cluster: ClusterInfo) -> Boolean)?

    /**
     * Called when an individual cluster item is clicked.
     * @return true if the event was consumed.
     */
    var onClusterItemClick: ((item: ClusterItem) -> Boolean)?

    /**
     * Called when clustering computation finishes.
     */
    var onClusterFinish: (() -> Unit)?
}
