package com.seraphim.core.map.amap

import android.content.Context
import android.location.LocationManager
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AMapUserLocationProvider(ctx: Context) : UserLocationProvider {
    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isLocationEnabled: Boolean
        get() = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    override val lastKnownLocation: UserPosition? = null

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        // Use AMap's own location SDK or Android built-in
        // TODO: Integrate com.amap.api.location when dependency added
    }

    override fun removeLocationUpdates(cb: LocationCallback) {}

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        // TODO: Implement with AMap Location SDK
        awaitClose {}
    }
}
