package com.seraphim.core.map.tmap

import android.content.Context
import android.location.LocationManager
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import com.skt.Tmap.TMapGpsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TmapUserLocationProvider(context: Context) : UserLocationProvider {

    private val gpsManager: TMapGpsManager = TMapGpsManager(context)
    private val systemLocationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var callback: LocationCallback? = null

    override val isLocationEnabled: Boolean
        get() = try {
            systemLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }

    override val lastKnownLocation: UserPosition? = null

    override fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long) {
        this.callback = callback
        gpsManager.setProvider(TMapGpsManager.GPS_PROVIDER)
        gpsManager.setMinTime(intervalMs.toInt())
        gpsManager.openGps()
        // TMapGpsManager provides location via onLocationChange callback
        // which needs to be set up by the implementing activity
        callback.onLocationResult(
            LocationResult.Success(
                UserPosition(
                    location = LatLng(
                        gpsManager.latitude,
                        gpsManager.longitude
                    )
                )
            )
        )
    }

    override fun removeLocationUpdates(callback: LocationCallback) {
        gpsManager.closeGps()
        this.callback = null
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        gpsManager.setProvider(TMapGpsManager.GPS_PROVIDER)
        gpsManager.openGps()
        // Poll location periodically
        kotlinx.coroutines.delay(1000)
        trySend(
            LocationResult.Success(
                UserPosition(location = LatLng(gpsManager.latitude, gpsManager.longitude))
            )
        )
        awaitClose { gpsManager.closeGps() }
    }
}
