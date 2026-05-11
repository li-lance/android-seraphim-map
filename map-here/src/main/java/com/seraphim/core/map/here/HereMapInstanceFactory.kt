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

class HereMapInstanceFactory : MapInstanceFactory {
    override val providerId = "here"

    override suspend fun checkAvailability(ctx: Context): MapAvailability {
        return try {
            Class.forName("com.here.sdk.mapview.MapView"); MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable("HERE SDK not found")
        }
    }

    override fun createMapHost(ctx: Context): MapHost =
        throw UnsupportedOperationException("HERE requires ViewGroup")

    override fun createMapHost(ctx: Context, parent: ViewGroup): MapHost =
        HereMapHost.create(ctx, parent)

    override fun createMapInstance(ctx: Context, opts: MapOptions): MapInstance = HereMapInstance()
    override fun createUserLocationProvider(ctx: Context): UserLocationProvider =
        HereUserLocationProvider(ctx)

    override fun createLocationDecoder(ctx: Context): LocationDecoder = HereLocationDecoder(ctx)
}
