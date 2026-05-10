package com.seraphim.core.map.commons.model

/**
 * Information about a cluster of items on the map.
 */
data class ClusterInfo(
    /** The items contained in this cluster. */
    val items: List<ClusterItem>,
    /** The geographical center of the cluster. */
    val position: LatLng,
    /** The number of items in this cluster. */
    val size: Int
)
