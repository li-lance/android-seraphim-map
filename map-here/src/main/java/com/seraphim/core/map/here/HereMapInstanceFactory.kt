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

    override suspend fun checkAvailability(context: Context): MapAvailability {
        return try {
            Class.forName("com.here.sdk.mapview.MapView")
            val engine = Class.forName("com.here.sdk.core.engine.SDKNativeEngine")
                .getMethod("getSharedInstance").invoke(null)
            if (engine == null) MapAvailability.Unavailable(
                "HERE SDK not initialized. Call HereMapHost.initSDK() in Application."
            )
            else MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable("HERE SDK not found")
        }
    }

    override fun createMapHost(context: Context): MapHost =
        throw UnsupportedOperationException("HERE requires ViewGroup")

    override fun createMapHost(context: Context, parent: ViewGroup): MapHost =
        HereMapHost.create(context, parent)

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance =
        HereMapInstance()

    override fun createUserLocationProvider(context: Context): UserLocationProvider =
        HereUserLocationProvider(context)

    override fun createLocationDecoder(context: Context): LocationDecoder =
        HereLocationDecoder(context)
}
