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

class HereUserLocationProvider(ctx: Context) : UserLocationProvider {
    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isLocationEnabled: Boolean
        get() = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    override val lastKnownLocation: UserPosition? = null

    @androidx.annotation.RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalMs, 0f) { loc ->
                cb.onLocationResult(
                    LocationResult.Success(
                        UserPosition(
                            LatLng(loc.latitude, loc.longitude), loc.bearing, loc.accuracy, loc.time
                        )
                    )
                )
            }
        } catch (e: SecurityException) {
            cb.onLocationResult(LocationResult.PermissionDenied)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        // Simplified: stop all
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow { awaitClose {} }
}
