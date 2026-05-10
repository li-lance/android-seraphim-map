package com.seraphim.core.map.commons.model

/**
 * Represents a geographical location with latitude and longitude.
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun from(lat: Double, lng: Double) = LatLng(lat, lng)
    }
}
