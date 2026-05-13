package com.seraphim.core.map.here

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class HereUserLocationProvider(
    private val context: Context,
    private val config: HereLocationConfig = HereLocationConfig()
) : UserLocationProvider {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val callbacks = mutableMapOf<LocationCallback, android.location.LocationListener>()

    override val isLocationEnabled: Boolean
        get() = LocationManagerCompat.isLocationEnabled(locationManager)

    override var lastKnownLocation: UserPosition? = null
        private set

    /**
     * Request a single location fix.
     *
     * Strategy: try cached [lastKnownLocation] first, then fall back to fresh location via flow.
     */
    override suspend fun requestSingleLocation(timeoutMs: Long): LocationResult {
        lastKnownLocation?.let {
            Log.d(TAG, "Returning cached last location")
            return LocationResult.Success(it)
        }

        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: LocationResult.Timeout
    }

    @androidx.annotation.RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long) {
        val provider = resolveProvider()
        val effectiveInterval = if (intervalMs > 0) intervalMs else config.intervalMs
        val listener = android.location.LocationListener { loc ->
            val position = loc.toUserPosition()
            lastKnownLocation = position
            callback.onLocationResult(LocationResult.Success(position))
        }
        callbacks[callback] = listener
        try {
            locationManager.requestLocationUpdates(
                provider,
                effectiveInterval,
                config.minDistanceMeters,
                listener
            )
        } catch (e: SecurityException) {
            callbacks.remove(callback)
            callback.onLocationResult(LocationResult.PermissionDenied)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        callbacks.remove(cb)?.let { locationManager.removeUpdates(it) }
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        val provider = resolveProvider()
        val listener = android.location.LocationListener { loc ->
            val position = loc.toUserPosition()
            lastKnownLocation = position
            trySend(LocationResult.Success(position))
        }
        try {
            locationManager.requestLocationUpdates(
                provider,
                config.intervalMs,
                config.minDistanceMeters,
                listener
            )
        } catch (e: SecurityException) {
            trySend(LocationResult.PermissionDenied)
        }
        awaitClose { locationManager.removeUpdates(listener) }
    }

    private fun resolveProvider(): String {
        return when (config.provider) {
            HereLocationProvider.GPS -> LocationManager.GPS_PROVIDER
            HereLocationProvider.Network -> LocationManager.NETWORK_PROVIDER
            HereLocationProvider.Passive -> LocationManager.PASSIVE_PROVIDER
        }
    }

    private fun android.location.Location.toUserPosition() = UserPosition(
        location = LatLng(latitude, longitude),
        bearing = bearing,
        accuracy = accuracy,
        timestamp = time
    )

    companion object {
        private const val TAG = "HereUserLocation"
    }
}
