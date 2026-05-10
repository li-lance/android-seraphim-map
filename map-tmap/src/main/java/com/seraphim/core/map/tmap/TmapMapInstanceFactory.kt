package com.seraphim.core.map.tmap

import android.content.Context
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory

class TmapMapInstanceFactory : MapInstanceFactory {

    override val providerId: String = "tmap"

    override suspend fun checkAvailability(context: Context): MapAvailability {
        return try {
            Class.forName("com.skt.Tmap.TMapView")
            MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable(
                reason = "Tmap SDK classes not found. Download AAR from https://tmapapi.tmapmobility.com",
                resolution = "Place the AAR in app/libs/ and add implementation(fileTree(" libs ") { include(" * . aar ") })"
            )
        }
    }

    override fun createMapHost(context: Context): MapHost {
        throw UnsupportedOperationException("Tmap MapHost requires a ViewGroup parent.")
    }

    fun createMapHost(context: Context, parent: ViewGroup): MapHost {
        return TmapMapHost.create(context, parent)
    }

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance {
        return TmapMapInstance()
    }

    override fun createUserLocationProvider(context: Context): UserLocationProvider {
        return TmapUserLocationProvider(context)
    }

    override fun createLocationDecoder(context: Context): LocationDecoder {
        return TmapLocationDecoder(context)
    }
}
