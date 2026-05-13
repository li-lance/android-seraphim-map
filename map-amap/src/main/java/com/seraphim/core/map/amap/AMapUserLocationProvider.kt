package com.seraphim.core.map.amap

import android.content.Context
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
    ctx: Context,
    private val config: AMapLocationConfig = AMapLocationConfig()
) : UserLocationProvider {

    private val client: AMapLocationClient = AMapLocationClient(ctx)
    private val option: AMapLocationClientOption
        get() = AMapLocationClientOption().apply {
            locationMode = when (config.locationMode) {
                AMapLocationMode.Hight_Accuracy -> AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                AMapLocationMode.Battery_Saving -> AMapLocationClientOption.AMapLocationMode.Battery_Saving
                AMapLocationMode.Device_Sensors -> AMapLocationClientOption.AMapLocationMode.Device_Sensors
            }
            interval = config.intervalMs.toInt().toLong()
            isOnceLocation = config.onceLocation
            isNeedAddress = config.needAddress
            isLocationCacheEnable = config.cacheEnabled
            isMockEnable = config.mockEnable
        }

    override val isLocationEnabled: Boolean = true
    override var lastKnownLocation: UserPosition? = null
        private set

    private var listener: AMapLocationListener? = null

    override suspend fun requestSingleLocation(timeoutMs: Long): LocationResult {
        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: LocationResult.Timeout
    }

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        client.setLocationOption(option)
        listener = AMapLocationListener { loc ->
            if (loc == null || loc.errorCode != 0) {
                cb.onLocationResult(LocationResult.Timeout)
                return@AMapLocationListener
            }
            val position = UserPosition(
                location = LatLng(loc.latitude, loc.longitude),
                bearing = loc.bearing.toFloat(),
                accuracy = loc.accuracy,
                timestamp = loc.time
            )
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
        client.setLocationOption(option)
        client.setLocationListener { loc ->
            if (loc != null && loc.errorCode == 0) {
                trySend(
                    LocationResult.Success(
                        UserPosition(
                            location = LatLng(loc.latitude, loc.longitude),
                            bearing = loc.bearing.toFloat(),
                            accuracy = loc.accuracy,
                            timestamp = loc.time
                        )
                    )
                )
            }
        }
        client.startLocation()
        awaitClose { client.stopLocation() }
    }
}
