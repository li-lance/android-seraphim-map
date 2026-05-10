package com.seraphim.core.map.here

import android.content.Context
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory

/**
 * [MapInstanceFactory] for HERE Maps.
 *
 * HERE SDK does not require special availability checks like Google Play Services.
 * However, it requires SDK initialization with access key/secret, which should
 * be performed by the host application before using this factory.
 */
class HereMapInstanceFactory : MapInstanceFactory {

    override val providerId: String = "here"

    override suspend fun checkAvailability(context: Context): MapAvailability {
        // HERE SDK is bundled with the APK — always available if the dependency exists
        return try {
            // Verify the SDK classes are accessible
            Class.forName("com.here.sdk.mapview.MapView")
            MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable(
                reason = "HERE SDK classes not found",
                resolution = "Ensure here-sdk dependency is included in the build"
            )
        }
    }

    override fun createMapHost(context: Context): MapHost {
        // HERE requires a ViewGroup parent to attach the MapView
        // Return a MapHost that creates the MapView lazily
        throw UnsupportedOperationException(
            "HERE MapHost requires a ViewGroup parent. Use createMapHost(context, parent) instead."
        )
    }

    /**
     * Create a [HereMapHost] by attaching a [com.here.sdk.mapview.MapView]
     * to the given [parent] [ViewGroup].
     */
    fun createMapHost(context: Context, parent: ViewGroup): MapHost {
        return HereMapHost.create(context, parent)
    }

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance {
        return HereMapInstance()
    }

    override fun createUserLocationProvider(context: Context): UserLocationProvider {
        return HereUserLocationProvider(context)
    }

    override fun createLocationDecoder(context: Context): LocationDecoder {
        return HereLocationDecoder(context)
    }
}
