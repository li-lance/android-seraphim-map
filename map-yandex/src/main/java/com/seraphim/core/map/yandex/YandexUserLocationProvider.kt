package com.seraphim.core.map.yandex

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import com.seraphim.core.map.commons.location.LocationCallback
import com.seraphim.core.map.commons.location.LocationResult
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.SubscriptionSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import com.yandex.mapkit.location.LocationManager as YandexLocationManager

class YandexUserLocationProvider(
    context: Context,
    private val config: YandexLocationConfig = YandexLocationConfig()
) : UserLocationProvider {

    private val yandexLocationManager: YandexLocationManager by lazy {
        com.yandex.mapkit.MapKitFactory.getInstance().createLocationManager()
    }
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val listeners = mutableMapOf<LocationCallback, LocationListener>()

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

    override fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long) {
        val subscription = SubscriptionSettings().apply {
            // TODO: apply config.useSatellite / config.autoPause if Yandex API supports
        }
        val listener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                val position = location.toUserPosition()
                lastKnownLocation = position
                callback.onLocationResult(LocationResult.Success(position))
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {}
        }
        listeners[callback] = listener
        yandexLocationManager.subscribeForLocationUpdates(subscription, listener)
    }

    override fun removeLocationUpdates(callback: LocationCallback) {
        listeners.remove(callback)?.let { yandexLocationManager.unsubscribe(it) }
    }

    override val locationFlow: Flow<LocationResult> = callbackFlow {
        val subscription = SubscriptionSettings()
        val listener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                val position = location.toUserPosition()
                lastKnownLocation = position
                trySend(LocationResult.Success(position))
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {}
        }
        yandexLocationManager.subscribeForLocationUpdates(subscription, listener)
        awaitClose { yandexLocationManager.unsubscribe(listener) }
    }

    private fun Location.toUserPosition() = UserPosition(
        location = LatLng(position.latitude, position.longitude)
    )

    companion object {
        private const val TAG = "YandexUserLocation"
    }
}
