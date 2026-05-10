package com.seraphim.core.map.commons.location

/**
 * Decodes geographical coordinates into addresses (reverse geocoding)
 * and addresses into coordinates (forward geocoding).
 *
 * Each map provider must implement this interface.
 */
interface LocationDecoder {
    /**
     * Reverse geocode: convert coordinates to an address.
     * Requires network access.
     */
    suspend fun reverseGeocode(request: GeocodingRequest): GeocodingResult

    /**
     * Forward geocode: convert an address string to coordinates.
     * Requires network access.
     */
    suspend fun forwardGeocode(
        query: String,
        language: String = "en",
        maxResults: Int = 5
    ): List<GeocodingAddress>
}
