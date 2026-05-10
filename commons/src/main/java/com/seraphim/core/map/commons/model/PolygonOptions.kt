package com.seraphim.core.map.commons.model

/**
 * Configuration for adding a polygon to the map.
 */
data class PolygonOptions(
    val points: List<LatLng>,
    val fillColor: Int,
    val strokeColor: Int = fillColor,
    val strokeWidth: Float = 1f,
    val zIndex: Float = 0f,
    val clickable: Boolean = false,
    val visible: Boolean = true
)
