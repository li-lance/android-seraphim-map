package com.seraphim.core.map.amap

import android.content.Context
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory

class AMapMapInstanceFactory : MapInstanceFactory {
    override val providerId = "amap"

    override suspend fun checkAvailability(ctx: Context): MapAvailability {
        return try {
            Class.forName("com.amap.api.maps.MapView"); MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable("AMap SDK not found", "Add com.amap.api:3dmap dependency")
        }
    }

    override fun createMapHost(ctx: Context): MapHost =
        throw UnsupportedOperationException("AMap requires ViewGroup")

    override fun createMapHost(ctx: Context, parent: ViewGroup): MapHost =
        AMapMapHost.create(ctx, parent)

    override fun createMapInstance(ctx: Context, opts: MapOptions): MapInstance = AMapMapInstance()
    fun createClusterableMapInstance(ctx: Context, opts: MapOptions): AMapClusterableMap =
        AMapClusterableMap()

    override fun createUserLocationProvider(ctx: Context): UserLocationProvider =
        AMapUserLocationProvider(ctx)

    override fun createLocationDecoder(ctx: Context): LocationDecoder = AMapLocationDecoder(ctx)
}
