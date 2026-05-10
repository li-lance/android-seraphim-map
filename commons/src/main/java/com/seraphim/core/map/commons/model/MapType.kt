package com.seraphim.core.map.commons.model

/**
 * Types of map display.
 */
enum class MapType {
    /** No tiles. */
    NONE,

    /** Standard map with roads and labels. */
    NORMAL,

    /** Satellite imagery. */
    SATELLITE,

    /** Hybrid (satellite + labels). */
    HYBRID,

    /** Terrain map with contours. */
    TERRAIN
}
