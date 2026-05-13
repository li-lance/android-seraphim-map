package com.seraphim.core.map.amap

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
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

class AMapUserLocationProvider(
    private val context: Context,
    private val config: AMapLocationConfig = AMapLocationConfig()
) : UserLocationProvider {

    private val client: AMapLocationClient = AMapLocationClient(context)
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isLocationEnabled: Boolean
        get() = LocationManagerCompat.isLocationEnabled(locationManager)

    override var lastKnownLocation: UserPosition? = null
        private set

    private var listener: AMapLocationListener? = null

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
        val option = buildOption(intervalMs)
        client.setLocationOption(option)
        listener = AMapLocationListener { loc ->
            if (loc == null || loc.errorCode != 0) {
                cb.onLocationResult(LocationResult.Timeout)
                return@AMapLocationListener
            }
            val position = loc.toUserPosition()
            lastKnownLocation = position
            cb.onLocationResult(LocationResult.Success(position))
        }
        client.setLocationListener(listener)
        client.startLocation()
    }

    override fun removeLocationUpdates(cb: LocationCallback) {
        client.stopLocation()
        listener = null
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        val option = buildOption(config.intervalMs)
        client.setLocationOption(option)
        client.setLocationListener { loc ->
            if (loc != null && loc.errorCode == 0) {
                val position = loc.toUserPosition()
                lastKnownLocation = position
                trySend(LocationResult.Success(position))
            }
        }
        client.startLocation()
        awaitClose { client.stopLocation() }
    }

    private fun buildOption(intervalMs: Long): AMapLocationClientOption {
        return AMapLocationClientOption().apply {
            locationMode = when (config.locationMode) {
                AMapLocationMode.Hight_Accuracy -> AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                AMapLocationMode.Battery_Saving -> AMapLocationClientOption.AMapLocationMode.Battery_Saving
                AMapLocationMode.Device_Sensors -> AMapLocationClientOption.AMapLocationMode.Device_Sensors
            }
            this.interval = if (intervalMs > 0) intervalMs else config.intervalMs
            isOnceLocation = if (intervalMs > 0) false else config.onceLocation
            isNeedAddress = config.needAddress
            isLocationCacheEnable = config.cacheEnabled
            isMockEnable = config.mockEnable
        }
    }

    private fun com.amap.api.location.AMapLocation.toUserPosition() = UserPosition(
        location = LatLng(latitude, longitude),
        bearing = bearing.toFloat(),
        accuracy = accuracy,
        timestamp = time
    )

    companion object {
        private const val TAG = "AMapUserLocation"
    }
}
