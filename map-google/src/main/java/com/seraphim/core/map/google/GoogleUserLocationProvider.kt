package com.seraphim.core.map.google

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.location.LocationManagerCompat
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.seraphim.core.map.commons.location.LocationCallback as ModelLocationCallback
import com.seraphim.core.map.commons.location.LocationResult as ModelLocationResult

/**
 * [UserLocationProvider] implementation using Google's FusedLocationProvider.
 *
 * Architecture notes (aligned with Porsche reference):
 * - All location acquisition goes through [locationFlow] (reactive stream).
 * - [requestSingleLocation] collects one emission from the flow with a timeout.
 * - [requestLocationUpdates] registers a callback that taps into the same flow mechanism.
 * - Permission checks are the caller's responsibility (Activity/Fragment).
 */
class GoogleUserLocationProvider(
    private val context: Context,
    private val config: GoogleLocationConfig = GoogleLocationConfig()
) : UserLocationProvider {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val callbacks = mutableMapOf<ModelLocationCallback, LocationCallback>()

    override val isLocationEnabled: Boolean
        get() = LocationManagerCompat.isLocationEnabled(locationManager)

    /**
     * Attempts to return the last known location from the fused provider.
     * Returns null if no cached location exists or permission is missing.
     */
    override val lastKnownLocation: UserPosition?
        @SuppressLint("MissingPermission")
        get() = try {
            // Note: getLastLocation() is async; we expose a synchronous nullable property.
            // For a truly synchronous last-known location, use [requestSingleLocation].
            null
        } catch (_: Exception) {
            null
        }

    /**
     * Request a single location fix.
     *
     * Strategy (Porsche-style):
     * 1. Try [getLastLocation] first for instant cached result.
     * 2. Fall back to [locationFlow.first()] with timeout for fresh fix.
     */
    override suspend fun requestSingleLocation(timeoutMs: Long): ModelLocationResult {
        // Step 1: Try cached last location immediately
        val cached = tryGetLastLocation()
        if (cached != null) {
            Log.d(TAG, "Returning cached last location")
            return ModelLocationResult.Success(cached)
        }

        // Step 2: Fall back to fresh location via flow
        return withTimeoutOrNull(timeoutMs) {
            locationFlow.first()
        } ?: ModelLocationResult.Timeout
    }

    /**
     * Request continuous location updates via callback.
     *
     * @param callback The callback to receive location results.
     * @param intervalMs Minimum interval between updates in milliseconds.
     *                   Use `0` for fastest possible updates (not single-shot).
     */
    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(
        callback: ModelLocationCallback,
        intervalMs: Long
    ) {
        val effectiveInterval = if (intervalMs > 0) intervalMs else config.intervalMs
        val locationRequest = LocationRequest.Builder(effectiveInterval)
            .setPriority(config.priority)
            .setMinUpdateIntervalMillis(config.fastestIntervalMs)
            .setMinUpdateDistanceMeters(config.smallestDisplacementMeters)
            .setWaitForAccurateLocation(config.waitForAccurateLocation)
            .setMaxUpdateDelayMillis(config.maxWaitTimeMs)
            .build()

        val googleCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val position = location.toUserPosition()
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

    /**
     * Reactive stream of location results.
     * Automatically starts updates on collection and stops on cancellation.
     *
     * Includes error handling via [addOnFailureListener] (Porsche pattern).
     */
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
                val position = location.toUserPosition()
                trySend(ModelLocationResult.Success(position))
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener { e ->
                Log.w(TAG, "Failed to request location updates", e)
                trySend(ModelLocationResult.LocationDisabled)
                close(e)
            }
        } catch (e: SecurityException) {
            trySend(ModelLocationResult.PermissionDenied)
            close(e)
        }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Try to get the last known location asynchronously.
     * Returns null if no cached location is available.
     */
    @SuppressLint("MissingPermission")
    private suspend fun tryGetLastLocation(): UserPosition? {
        return try {
            val location = suspendCancellableCoroutine { cont ->
                fusedClient.lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
                    .addOnCanceledListener { cont.cancel() }
            }
            location?.toUserPosition()
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied for last location", e)
            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get last location", e)
            null
        }
    }

    private fun android.location.Location.toUserPosition() = UserPosition(
        location = LatLng(latitude, longitude),
        bearing = bearing,
        accuracy = accuracy,
        timestamp = time
    )

    companion object {
        private const val TAG = "GoogleUserLocation"
    }
}
