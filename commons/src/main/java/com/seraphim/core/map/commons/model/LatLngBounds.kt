package com.seraphim.core.map.commons.model

/**
 * Represents a rectangular geographical area defined by southwest and northeast corners.
 */
data class LatLngBounds(
    val southwest: LatLng,
    val northeast: LatLng
) {
    val center: LatLng
        get() = LatLng(
            latitude = (southwest.latitude + northeast.latitude) / 2.0,
            longitude = (southwest.longitude + northeast.longitude) / 2.0
        )
}
