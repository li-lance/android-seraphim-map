package com.seraphim.core.map.here

import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.here.sdk.location.Location
import com.here.sdk.location.LocationEngine
import com.here.sdk.location.LocationListener
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.seraphim.core.map.commons.location.LocationCallback as ModelLocationCallback
import com.seraphim.core.map.commons.location.LocationResult as ModelLocationResult

/**
 * [UserLocationProvider] implementation using HERE SDK [LocationEngine].
 */
class HereUserLocationProvider(
    private val context: Context
) : UserLocationProvider {

    private val locationEngine: LocationEngine = LocationEngine()

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val isLocationEnabled: Boolean
        get() {
            return try {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                false
            }
        }

    override val lastKnownLocation: UserPosition? = null
    // HERE SDK LocationEngine provides last known via start() callback

    override fun requestLocationUpdates(
        callback: ModelLocationCallback,
        intervalMs: Long
    ) {
        try {
            locationEngine.start(
                object : LocationListener {
                    override fun onLocationUpdated(location: Location) {
                        val position = UserPosition(
                            location = LatLng(
                                location.coordinates.latitude,
                                location.coordinates.longitude
                            ),
                            bearing = location.bearingInDegrees?.toFloat() ?: 0f,
                            accuracy = location.horizontalAccuracyInMeters?.toFloat() ?: 0f,
                            timestamp = location.timestamp.time
                        )
                        callback.onLocationResult(ModelLocationResult.Success(position))
                    }
                },
                com.here.sdk.location.LocationAccuracy.BEST_AVAILABLE
            )
        } catch (e: Exception) {
            Log.w(TAG, "Location updates failed", e)
            callback.onLocationResult(ModelLocationResult.LocationDisabled)
        }
    }

    override fun removeLocationUpdates(callback: ModelLocationCallback) {
        try {
            locationEngine.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop location engine", e)
        }
    }

    override val locationFlow: Flow<ModelLocationResult> = callbackFlow {
        locationEngine.start(
            object : LocationListener {
                override fun onLocationUpdated(location: Location) {
                    val position = UserPosition(
                        location = LatLng(
                            location.coordinates.latitude,
                            location.coordinates.longitude
                        ),
                        bearing = location.bearingInDegrees?.toFloat() ?: 0f,
                        accuracy = location.horizontalAccuracyInMeters?.toFloat() ?: 0f,
                        timestamp = location.timestamp.time
                    )
                    trySend(ModelLocationResult.Success(position))
                }
            },
            com.here.sdk.location.LocationAccuracy.BEST_AVAILABLE
        )

        awaitClose {
            locationEngine.stop()
        }
    }

    companion object {
        private const val TAG = "HereUserLocation"
    }
}
