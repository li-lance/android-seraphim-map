package com.seraphim.core.map.tmap

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import com.skt.tmap.TMapGpsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class TmapUserLocationProvider(
    private val context: Context,
    private val config: TmapLocationConfig = TmapLocationConfig()
) : UserLocationProvider {

    private val gps = TMapGpsManager(context)
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val callbacks =
        mutableMapOf<LocationCallback, TMapGpsManager.OnLocationChangedListener>()

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

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        val listener = TMapGpsManager.OnLocationChangedListener { point ->
            val position = UserPosition(
                location = LatLng(point.latitude, point.longitude),
                bearing = point.rotation
            )
            lastKnownLocation = position
            cb.onLocationResult(LocationResult.Success(position))
        }
        callbacks[cb] = listener
        gps.setOnLocationChangeListener(listener)
        gps.setProvider(
            when (config.provider) {
                TmapLocationProvider.GPS -> TMapGpsManager.PROVIDER_GPS
                TmapLocationProvider.Network -> TMapGpsManager.PROVIDER_NETWORK
            }
        )
        gps.setMinTime(if (intervalMs > 0) intervalMs else config.intervalMs)
        try {
            gps.openGps()
        } catch (e: Exception) {
            callbacks.remove(cb)
            gps.setOnLocationChangeListener(null)
            cb.onLocationResult(LocationResult.LocationDisabled)
        }
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        callbacks.remove(cb)
        if (callbacks.isEmpty()) {
            gps.setOnLocationChangeListener(null)
            gps.closeGps()
        }
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        val listener = TMapGpsManager.OnLocationChangedListener { point ->
            val position = UserPosition(
                location = LatLng(point.latitude, point.longitude),
                bearing = point.rotation
            )
            lastKnownLocation = position
            trySend(LocationResult.Success(position))
        }
        gps.setOnLocationChangeListener(listener)
        gps.setProvider(
            when (config.provider) {
                TmapLocationProvider.GPS -> TMapGpsManager.PROVIDER_GPS
                TmapLocationProvider.Network -> TMapGpsManager.PROVIDER_NETWORK
            }
        )
        gps.setMinTime(config.intervalMs)
        try {
            gps.openGps()
        } catch (e: Exception) {
            trySend(LocationResult.LocationDisabled)
        }
        awaitClose {
            gps.setOnLocationChangeListener(null)
            gps.closeGps()
        }
    }

    companion object {
        private const val TAG = "TmapUserLocation"
    }
}
