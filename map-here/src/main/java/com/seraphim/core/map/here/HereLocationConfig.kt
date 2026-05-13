package com.seraphim.core.map.here

/**
 * HERE Maps location-specific configuration.
 *
 * @param intervalMs Desired interval between location updates.
 * @param provider Location provider: GPS, network, or passive.
 * @param minDistanceMeters Minimum distance between updates.
 */
data class HereLocationConfig(
    val intervalMs: Long = 5000,
    val provider: HereLocationProvider = HereLocationProvider.GPS,
    val minDistanceMeters: Float = 0f
)

enum class HereLocationProvider {
    GPS,
    Network,
    Passive
}
