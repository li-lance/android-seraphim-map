package com.seraphim.core.map.yandex

import android.content.Context
import android.location.LocationManager
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
import com.yandex.mapkit.location.LocationManager as YandexLocationManager

class YandexUserLocationProvider(context: Context) : UserLocationProvider {

    private val yandexLocationManager: YandexLocationManager by lazy {
        com.yandex.mapkit.MapKitFactory.getInstance().createLocationManager()
    }
    private val systemLocationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val listeners = mutableMapOf<LocationCallback, LocationListener>()

    override val isLocationEnabled: Boolean
        get() = try {
            systemLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    systemLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }

    override val lastKnownLocation: UserPosition? = null

    override fun requestLocationUpdates(callback: LocationCallback, intervalMs: Long) {
        val subscription = SubscriptionSettings().apply {
            // TODO: Yandex 4.33.1 SubscriptionSettings API may differ.
            // Adjust interval configuration as needed.
        }
        val listener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                val position = UserPosition(
                    location = LatLng(
                        location.position.latitude,
                        location.position.longitude
                    )
                )
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
                trySend(
                    LocationResult.Success(
                        UserPosition(
                            location = LatLng(
                                location.position.latitude,
                                location.position.longitude
                            )
                        )
                    )
                )
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {}
        }
        yandexLocationManager.subscribeForLocationUpdates(subscription, listener)
        awaitClose { yandexLocationManager.unsubscribe(listener) }
    }
}
