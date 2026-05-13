package com.seraphim.core.map.commons.location

import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.flow.Flow

/**
 * Provides user location data.
 *
 * Each map provider must implement this interface using its own
 * location engine (e.g., FusedLocationProvider for Google, AMapLocationClient for AMap, etc.).
 *
 * The provider supports three usage patterns:
 * 1. **Single location** — [requestSingleLocation] for one-shot queries.
 * 2. **Continuous updates** — [requestLocationUpdates] for streaming via callback.
 * 3. **Reactive stream** — [locationFlow] for Flow-based consumption.
 *
 * All methods handle permission checks internally and return [LocationResult] to signal errors.
 *
 * Each provider may expose its own configuration class (e.g., [GoogleLocationConfig],
 * [AMapLocationConfig]) to customize provider-specific parameters.
 */
interface UserLocationProvider {

    /**
     * Request a single location fix.
     *
     * @param timeoutMs Maximum time to wait for a location fix.
     * @return [LocationResult.Success] with the position, or an error result.
     */
    suspend fun requestSingleLocation(timeoutMs: Long = 10000): LocationResult

    /**
     * Request continuous location updates via callback.
     *
     * @param callback The callback to receive location results.
     * @param intervalMs Minimum interval between updates in milliseconds.
     *                   Use `0` for single-shot (one update then auto-stop).
     */
    fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long = 5000)

    /**
     * Stop receiving location updates for the given callback.
     */
    fun removeLocationUpdates(callback: LocationCallback)

    /**
     * A [Flow] of [LocationResult] for reactive consumers.
     * The flow automatically starts location updates on collection and stops on cancellation.
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
