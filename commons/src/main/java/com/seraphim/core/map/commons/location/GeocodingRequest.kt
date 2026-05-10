package com.seraphim.core.map.commons.location

import com.seraphim.core.map.commons.model.LatLng

/**
 * Request for reverse geocoding (coordinates to address).
 */
data class GeocodingRequest(
    /** The location to decode. */
    val location: LatLng,
    /** Preferred language for the result (e.g., "en", "zh-CN"). */
    val language: String = "en",
    /** Maximum number of results to return. */
    val maxResults: Int = 1
)
