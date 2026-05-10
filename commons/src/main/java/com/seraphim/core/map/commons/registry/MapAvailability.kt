package com.seraphim.core.map.commons.registry

/**
 * Result of checking whether a map provider is available on the device.
 */
sealed class MapAvailability {
    /** The provider is available. */
    object Available : MapAvailability()

    /**
     * The provider is unavailable.
     * @param reason Human-readable reason.
     * @param resolution Suggested resolution steps, or null.
     */
    data class Unavailable(val reason: String, val resolution: String? = null) : MapAvailability()
}
