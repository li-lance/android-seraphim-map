package com.seraphim.core.map.commons.location

import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.flow.Flow

/**
 * Provides user location data.
 *
 * Each map provider must implement this interface using its own
 * location engine (e.g., FusedLocationProvider for Google, positioning
 * module for HERE, etc.).
 */
interface UserLocationProvider {
    /**
     * Request continuous location updates via callback.
     * @param callback The callback to receive location results.
     * @param intervalMs Minimum interval between updates in milliseconds.
     */
    fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long = 5000)

    /**
     * Stop receiving location updates for the given callback.
     */
    fun removeLocationUpdates(callback: LocationCallback)

    /**
     * A [Flow] of [LocationResult] for reactive consumers.
     */
    val locationFlow: Flow<LocationResult>

    /**
     * The last known user position, or null if unavailable.
     */
    val lastKnownLocation: UserPosition?

    /**
     * Whether location is enabled in device settings.
     */
    val isLocationEnabled: Boolean
}
