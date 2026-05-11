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

class AMapUserLocationProvider(ctx: Context) : UserLocationProvider {

    private val client: AMapLocationClient = AMapLocationClient(ctx)
    private val option = AMapLocationClientOption().apply {
        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        isOnceLocation = false
    }

    override val isLocationEnabled: Boolean = true // AMap SDK handles internally
    override var lastKnownLocation: UserPosition? = null
        private set

    private var listener: AMapLocationListener? = null

    override fun requestLocationUpdates(cb: LocationCallback, intervalMs: Long) {
        option.let {
            it.interval = intervalMs
            it.isOnceLocation = intervalMs == 0L  // single-shot when interval is 0
        }
        client.setLocationOption(option)
        listener = AMapLocationListener { loc ->
            if (loc == null) {
                cb.onLocationResult(LocationResult.Timeout)
                return@AMapLocationListener
            }
            if (loc.errorCode != 0) {
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
        option.interval = 5000
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
