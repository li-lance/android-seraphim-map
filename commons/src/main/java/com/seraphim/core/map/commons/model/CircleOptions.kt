package com.seraphim.core.map.commons.model

/**
 * Configuration for adding a circle to the map.
 */
data class CircleOptions(
    val center: LatLng,
    val radius: Double,
    val fillColor: Int,
    val strokeColor: Int = fillColor,
    val strokeWidth: Float = 1f,
    val zIndex: Float = 0f,
    val clickable: Boolean = false,
    val visible: Boolean = true
)
