package com.seraphim.core.map.commons.location

/**
 * Precision level of a geocoding result.
 * Availability depends on the provider and the location.
 */
enum class GeocodingPrecision {
    /** Unknown precision. */
    UNKNOWN,

    /** Country-level precision. */
    COUNTRY,

    /** Region/state-level precision. */
    REGION,

    /** City-level precision. */
    CITY,

    /** District-level precision. */
    DISTRICT,

    /** Street-level precision. */
    STREET,

    /** Exact address (house number). */
    ADDRESS,

    /** Point of interest. */
    POI
}
