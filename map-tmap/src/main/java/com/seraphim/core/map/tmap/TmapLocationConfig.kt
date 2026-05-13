package com.seraphim.core.map.tmap

/**
 * Tmap location-specific configuration.
 *
 * @param intervalMs Desired interval between location updates.
 * @param minDistanceMeters Minimum distance between updates.
 * @param provider GPS or network provider preference.
 */
data class TmapLocationConfig(
    val intervalMs: Long = 5000,
    val minDistanceMeters: Float = 0f,
    val provider: TmapLocationProvider = TmapLocationProvider.GPS
)

enum class TmapLocationProvider {
    GPS,
    Network
}
