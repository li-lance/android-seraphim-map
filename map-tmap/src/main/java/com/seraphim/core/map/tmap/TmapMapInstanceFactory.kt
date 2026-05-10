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
    override val providerId = "tmap"

    override suspend fun checkAvailability(ctx: Context): MapAvailability {
        return try {
            Class.forName("com.skt.tmap.TMapView")
            MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable("Tmap SDK not found", "Add tmap-sdk AAR to libs/")
        }
    }

    override fun createMapHost(ctx: Context): MapHost {
        throw UnsupportedOperationException("Tmap requires ViewGroup. Use createMapHost(ctx, parent, apiKey).")
    }

    fun createMapHost(ctx: Context, parent: ViewGroup, apiKey: String = ""): MapHost =
        TmapMapHost.create(ctx, parent, apiKey)

    override fun createMapInstance(ctx: Context, options: MapOptions): MapInstance =
        TmapMapInstance()

    fun createClusterableMapInstance(ctx: Context, options: MapOptions): TmapClusterableMap =
        TmapClusterableMap()

    override fun createUserLocationProvider(ctx: Context): UserLocationProvider =
        TmapUserLocationProvider(ctx)

    override fun createLocationDecoder(ctx: Context): LocationDecoder =
        TmapLocationDecoder(ctx)
}
