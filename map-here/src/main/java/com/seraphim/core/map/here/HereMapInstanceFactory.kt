package com.seraphim.core.map.here

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.seraphim.core.map.commons.MapCredentials
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapSdkInitializer
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory

class HereMapInstanceFactory : MapInstanceFactory, MapSdkInitializer {
    override val providerId = "here"

    override suspend fun checkAvailability(context: Context): MapAvailability {
        return try {
            Class.forName("com.here.sdk.mapview.MapView")
            val engine = Class.forName("com.here.sdk.core.engine.SDKNativeEngine")
                .getMethod("getSharedInstance").invoke(null)
            if (engine == null) MapAvailability.Unavailable(
                "HERE SDK not initialized. Call MapInitializer.init() in Application."
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

    // ── MapSdkInitializer ──

    override fun init(app: Application, credentials: MapCredentials) {
        val (accessKeyId, accessKeySecret) = when (credentials) {
            is MapCredentials.HereCredentials -> Pair(
                credentials.accessKeyId,
                credentials.accessKeySecret
            )

            else -> {
                Log.w("HERE", "HERE requires HereCredentials"); return
            }
        }
        try {
            val auth = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options = SDKOptions(auth)
            SDKNativeEngine.makeSharedInstance(app, options)
            Log.d("HERE", "HERE SDK initialized")
        } catch (e: Exception) {
            Log.w("HERE", "HERE SDK init failed: ${e.message}")
        }
    }
}
