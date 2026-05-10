package com.seraphim.core.map.commons.model

/**
 * Represents an item that can be clustered on the map.
 */
data class ClusterItem(
    val id: String,
    val position: LatLng,
    val title: String = "",
    val snippet: String = "",
    val icon: IconProvider? = null,
    val zIndex: Float = 0f,
    val tag: Any? = null
)
