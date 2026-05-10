package com.seraphim.core.map.commons.model

/**
 * Configuration for adding a polyline to the map.
 */
data class PolylineOptions(
    val points: List<LatLng>,
    val color: Int,
    val width: Float = 1f,
    val zIndex: Float = 0f,
    val clickable: Boolean = false,
    val visible: Boolean = true
)
