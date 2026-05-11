package com.seraphim.core.map.commons.model

/**
 * Represents a marker on the map. Returned when a marker is added.
 */
interface Marker {
    /** Unique identifier for this marker. */
    val id: String

    /** The current position of the marker. */
    var position: LatLng

    /** Whether the marker is visible. */
    var visible: Boolean

    /** Optional business data attached to this marker (e.g., charging station, POI). */
    var tag: Any?

    /** Remove this marker from the map. */
    fun remove()
}
