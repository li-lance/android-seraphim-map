package com.seraphim.core.map.commons.model

/**
 * Padding offsets for the map view in pixels.
 */
data class MapPadding(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) {
    companion object {
        val NONE = MapPadding(0, 0, 0, 0)
    }
}
