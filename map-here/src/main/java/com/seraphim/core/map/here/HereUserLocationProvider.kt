package com.seraphim.core.map.here

import android.Manifest
import android.content.Context
import android.location.LocationManager
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
    ctx: Context,
    private val config: HereLocationConfig = HereLocationConfig()
) : UserLocationProvider {
    override suspend fun requestSingleLocation(timeoutMs: Long): LocationResult {
        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: LocationResult.Timeout
    }

    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val callbacks = mutableMapOf<LocationCallback, android.location.LocationListener>()

    override val isLocationEnabled: Boolean
        get() = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    override val lastKnownLocation: UserPosition? = null

    @androidx.annotation.RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long) {
        val provider = when (config.provider) {
            HereLocationProvider.GPS -> LocationManager.GPS_PROVIDER
            HereLocationProvider.Network -> LocationManager.NETWORK_PROVIDER
            HereLocationProvider.Passive -> LocationManager.PASSIVE_PROVIDER
        }
        val listener = android.location.LocationListener { loc ->
            callback.onLocationResult(
                LocationResult.Success(
                    UserPosition(
                        LatLng(loc.latitude, loc.longitude), loc.bearing, loc.accuracy, loc.time
                    )
                )
            )
        }
        callbacks[callback] = listener
        try {
            lm.requestLocationUpdates(
                provider,
                config.intervalMs,
                config.minDistanceMeters,
                listener
            )
        } catch (e: SecurityException) {
            callbacks.remove(callback)
            callback.onLocationResult(LocationResult.PermissionDenied)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        callbacks.remove(cb)?.let { lm.removeUpdates(it) }
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow { awaitClose {} }
}
