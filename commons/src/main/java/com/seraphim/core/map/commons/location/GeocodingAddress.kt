package com.seraphim.core.map.commons.location

/**
 * A structured address returned by geocoding.
 * All fields are nullable — availability depends on the provider and location.
 */
data class GeocodingAddress(
    val latitude: Double,
    val longitude: Double,
    /** Full formatted address string. */
    val formattedAddress: String,

    // Country
    val country: String? = null,
    val countryCode: String? = null,

    // Administrative
    val region: String? = null,
    val city: String? = null,
    val district: String? = null,

    // Street
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,

    // POI
    val poiName: String? = null
)
