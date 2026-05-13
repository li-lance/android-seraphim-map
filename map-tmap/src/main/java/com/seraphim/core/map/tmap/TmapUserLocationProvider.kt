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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class TmapUserLocationProvider(
    ctx: Context,
    private val config: TmapLocationConfig = TmapLocationConfig()
) : UserLocationProvider {

    override suspend fun requestSingleLocation(timeoutMs: Long): LocationResult {
        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: LocationResult.Timeout
    }

    private val gps = TMapGpsManager(ctx)
    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var activeCallback: LocationCallback? = null

    override val isLocationEnabled: Boolean
        get() = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }

    override val lastKnownLocation: UserPosition? = null

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        activeCallback = cb
        try {
            gps.openGps()
        } catch (e: Exception) {
            activeCallback = null
            cb.onLocationResult(LocationResult.LocationDisabled)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        if (activeCallback == cb) {
            activeCallback = null
            gps.closeGps()
        }
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        trySend(LocationResult.LocationDisabled)
        awaitClose { gps.closeGps() }
    }
}
