package com.seraphim.core.map.google

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory

/**
 * [MapInstanceFactory] for Google Maps.
 *
 * Checks Google Play Services availability before creating instances.
 */
class GoogleMapInstanceFactory : MapInstanceFactory {

    override val providerId: String = "google"

    override suspend fun checkAvailability(context: Context): MapAvailability {
        val api = GoogleApiAvailability.getInstance()
        val result = api.isGooglePlayServicesAvailable(context)
        return if (result == ConnectionResult.SUCCESS) {
            MapAvailability.Available
        } else {
            val errorString = api.getErrorString(result)
            val resolution = when (result) {
                ConnectionResult.SERVICE_MISSING -> "Google Play Services is missing. Install from Play Store."
                ConnectionResult.SERVICE_UPDATING -> "Google Play Services is updating. Please wait."
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Update Google Play Services."
                ConnectionResult.SERVICE_DISABLED -> "Enable Google Play Services in Settings."
                ConnectionResult.SERVICE_INVALID -> "Google Play Services is invalid. Reinstall."
                else -> errorString
            }
            MapAvailability.Unavailable(
                reason = "Google Play Services error: $errorString (code: $result)",
                resolution = resolution
            )
        }
    }

    override fun createMapHost(context: Context): MapHost {
        val fragment = SupportMapFragment.newInstance()
        return GoogleMapHost(fragment)
    }

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance {
        return GoogleMapInstance()
    }

    /**
     * Create a [GoogleClusterableMap] with clustering support.
     */
    fun createClusterableMapInstance(context: Context, options: MapOptions): GoogleClusterableMap {
        return GoogleClusterableMap(context)
    }

    override fun createUserLocationProvider(context: Context): UserLocationProvider {
        return GoogleUserLocationProvider(context)
    }

    override fun createLocationDecoder(context: Context): LocationDecoder {
        return GoogleLocationDecoder(context)
    }
}
