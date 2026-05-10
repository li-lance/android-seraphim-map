package com.seraphim.core.map.commons.location

/**
 * Result of a geocoding request.
 */
data class GeocodingResult(
    /** The full formatted address (first result). */
    val formattedAddress: String?,
    /** All results from the geocoding request. */
    val results: List<GeocodingAddress>
)
