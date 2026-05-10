package com.seraphim.core.map.tmap

import android.content.Context
import android.location.LocationManager
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.UserPosition
import com.skt.tmap.TMapGpsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TmapUserLocationProvider(ctx: Context) : UserLocationProvider {

    private val gps = TMapGpsManager(ctx)
    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isLocationEnabled: Boolean
        get() = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }

    override val lastKnownLocation: UserPosition? = null

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        try {
            gps.openGps()
        } catch (e: Exception) {
            cb.onLocationResult(LocationResult.LocationDisabled)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        gps.closeGps()
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        trySend(LocationResult.LocationDisabled) // TMapGpsManager API differs in v3.5
        awaitClose { gps.closeGps() }
    }
}
