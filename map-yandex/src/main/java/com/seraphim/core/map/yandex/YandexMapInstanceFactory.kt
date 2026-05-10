package com.seraphim.core.map.yandex

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
 * [MapInstanceFactory] for Yandex MapKit.
 *
 * Yandex MapKit requires initialization before use:
 * ```kotlin
 * MapKitFactory.setApiKey("your_api_key")
 * MapKitFactory.initialize(context)
 * ```
 *
 * This should be done by the host application in Application.onCreate().
 */
class YandexMapInstanceFactory : MapInstanceFactory {

    override val providerId: String = "yandex"

    override suspend fun checkAvailability(context: Context): MapAvailability {
        return try {
            Class.forName("com.yandex.mapkit.MapKitFactory")
            MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable(
                reason = "Yandex MapKit classes not found",
                resolution = "Ensure yandex-mapkit dependency is included"
            )
        }
    }

    override fun createMapHost(context: Context): MapHost {
        throw UnsupportedOperationException(
            "Yandex MapHost requires a ViewGroup parent. Use createMapHost(context, parent)."
        )
    }

    fun createMapHost(context: Context, parent: ViewGroup): MapHost {
        return YandexMapHost.create(context, parent)
    }

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance {
        return YandexMapInstance()
    }

    fun createClusterableMapInstance(context: Context, options: MapOptions): YandexClusterableMap {
        return YandexClusterableMap()
    }

    override fun createUserLocationProvider(context: Context): UserLocationProvider {
        return YandexUserLocationProvider(context)
    }

    override fun createLocationDecoder(context: Context): LocationDecoder {
        return YandexLocationDecoder(context)
    }
}
