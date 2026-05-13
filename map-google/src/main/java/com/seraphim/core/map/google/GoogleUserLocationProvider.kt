package com.seraphim.core.map.google

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.UserPosition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import com.seraphim.core.map.commons.location.LocationCallback as ModelLocationCallback
import com.seraphim.core.map.commons.location.LocationResult as ModelLocationResult

/**
 * [UserLocationProvider] implementation using Google's FusedLocationProvider.
 */
class GoogleUserLocationProvider(
    private val context: Context,
    private val config: GoogleLocationConfig = GoogleLocationConfig()
) : UserLocationProvider {

    override suspend fun requestSingleLocation(timeoutMs: Long): ModelLocationResult {
        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: ModelLocationResult.Timeout
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val callbacks = mutableMapOf<ModelLocationCallback, LocationCallback>()

    override val isLocationEnabled: Boolean
        get() {
            return try {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check location status", e)
                false
            }
        }

    override val lastKnownLocation: UserPosition?
        @SuppressLint("MissingPermission")
        get() {
            return null
        }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(
        callback: ModelLocationCallback,
        intervalMs: Long
    ) {
        val locationRequest = LocationRequest.Builder(config.intervalMs)
            .setPriority(config.priority)
            .setMinUpdateIntervalMillis(config.fastestIntervalMs)
            .setMinUpdateDistanceMeters(config.smallestDisplacementMeters)
            .setWaitForAccurateLocation(config.waitForAccurateLocation)
            .setMaxUpdateDelayMillis(config.maxWaitTimeMs)
            .build()

        val googleCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val position = UserPosition(
                    location = LatLng(location.latitude, location.longitude),
                    bearing = location.bearing,
                    accuracy = location.accuracy,
                    timestamp = location.time
                )
                callback.onLocationResult(ModelLocationResult.Success(position))
            }
        }

        callbacks[callback] = googleCallback

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                googleCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission denied", e)
            callbacks.remove(callback)
            callback.onLocationResult(ModelLocationResult.PermissionDenied)
        }
    }

    @SuppressLint("MissingPermission")
    override fun removeLocationUpdates(callback: ModelLocationCallback) {
        callbacks.remove(callback)?.let { fusedClient.removeLocationUpdates(it) }
    }

    @SuppressLint("MissingPermission")
    override val locationFlow: Flow<ModelLocationResult> = callbackFlow {
        val locationRequest = LocationRequest.Builder(config.intervalMs)
            .setPriority(config.priority)
            .setMinUpdateIntervalMillis(config.fastestIntervalMs)
            .setMinUpdateDistanceMeters(config.smallestDisplacementMeters)
            .setWaitForAccurateLocation(config.waitForAccurateLocation)
            .setMaxUpdateDelayMillis(config.maxWaitTimeMs)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val position = UserPosition(
                    location = LatLng(location.latitude, location.longitude),
                    bearing = location.bearing,
                    accuracy = location.accuracy,
                    timestamp = location.time
                )
                trySend(ModelLocationResult.Success(position))
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            trySend(ModelLocationResult.PermissionDenied)
        }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    companion object {
        private const val TAG = "GoogleUserLocation"
    }
}
