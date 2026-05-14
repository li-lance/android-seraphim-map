package com.seraphim.core.map.google

import android.app.Application
import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.seraphim.core.map.commons.MapCredentials
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapSdkInitializer
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory
import com.seraphim.core.map.commons.search.PoiSearch

/**
 * [MapInstanceFactory] for Google Maps.
 *
 * Checks Google Play Services availability before creating instances.
 * SDK init initializes the Places SDK with the provided API key.
 */
class GoogleMapInstanceFactory : MapInstanceFactory, MapSdkInitializer {

    override val providerId: String = "google"

    private var credentials: MapCredentials = MapCredentials.None

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

    override fun createMapHost(context: Context, parent: ViewGroup): MapHost {
        // Use MapView-based host for ViewGroup attachment (MapFragment use case)
        return GoogleMapHost.createWithMapView(context, parent)
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

    override fun createPoiSearch(context: Context): PoiSearch {
        return GooglePoiSearch(context)
    }

    // ── MapSdkInitializer ──

    override fun init(app: Application, credentials: MapCredentials) {
        this.credentials = credentials
        val apiKey = when (credentials) {
            is MapCredentials.ApiKey -> credentials.key
            else -> null
        }
        if (!Places.isInitialized()) {
            val keyToUse = if (!apiKey.isNullOrBlank()) {
                apiKey
            } else {
                // Fallback: read from AndroidManifest meta-data
                readApiKeyFromManifest(app)
            }
            if (!keyToUse.isNullOrBlank()) {
                Places.initialize(app, keyToUse)
            } else {
                android.util.Log.w(
                    "GoogleMapInstanceFactory",
                    "No Places API key found. PoiSearch will fail."
                )
            }
        }
    }

    private fun readApiKeyFromManifest(context: Context): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            val key = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            android.util.Log.d(
                "GoogleMapInstanceFactory",
                "Read API key from manifest: ${key?.take(10)}..."
            )
            key
        } catch (e: Exception) {
            android.util.Log.e(
                "GoogleMapInstanceFactory",
                "Failed to read API key from manifest",
                e
            )
            null
        }
    }
}
