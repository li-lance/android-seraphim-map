package com.seraphim.core.map.commons.model

/**
 * Configuration for adding a marker to the map.
 */
data class MarkerOptions(
    val position: LatLng,
    val title: String = "",
    val snippet: String = "",
    val icon: IconProvider? = null,
    val anchor: Pair<Float, Float> = Pair(0.5f, 1.0f),
    val alpha: Float = 1f,
    val rotation: Float = 0f,
    val draggable: Boolean = false,
    val flat: Boolean = false,
    val zIndex: Float = 0f,
    val visible: Boolean = true,
    val tag: Any? = null
) {
    companion object {
        val ANCHOR_CENTER = Pair(0.5f, 0.5f)
        val ANCHOR_BOTTOM_CENTER = Pair(0.5f, 1.0f)
        val ANCHOR_TOP_CENTER = Pair(0.5f, 0.0f)
    }
}
