package com.seraphim.core.map.commons

import android.app.Application
import android.util.Log
import com.seraphim.core.map.commons.MapInitializer.init

/**
 * Provider SDK initialization. Call from [Application.onCreate].
 *
 * Usage:
 * ```
 * MapInitializer.init(this, MapProviders.AMAP)
 * val provider = MapInitializer.activeProvider  // "amap"
 * ```
 */
object MapInitializer {
    private const val TAG = "MapInitializer"

    /** The active provider ID, set during [init]. */
    var activeProvider: String = ""
        private set

    fun init(app: Application, providerId: String, apiKey: String? = null) {
        activeProvider = providerId
        when (providerId) {
            MapProviders.AMAP -> initAMap(app)
            MapProviders.YANDEX -> initYandex(app, apiKey)
            MapProviders.GOOGLE -> Log.d(TAG, "Google: ensure API key in AndroidManifest")
            else -> Log.d(TAG, "$providerId: init via provider SDK")
        }
    }

    private fun initAMap(app: Application) {
        try {
            val c = Class.forName("com.amap.api.maps.MapsInitializer")
            c.getMethod(
                "updatePrivacyShow",
                android.content.Context::class.java,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            )
                .invoke(null, app, true, true)
            c.getMethod(
                "updatePrivacyAgree",
                android.content.Context::class.java,
                Boolean::class.javaPrimitiveType
            )
                .invoke(null, app, true)
        } catch (e: Exception) {
            Log.w(TAG, "AMap SDK not found")
        }
    }

    private fun initYandex(app: Application, key: String?) {
        if (key == null) return
        try {
            val c = Class.forName("com.yandex.mapkit.MapKitFactory")
            c.getMethod("setApiKey", String::class.java).invoke(null, key)
            c.getMethod("initialize", android.content.Context::class.java).invoke(null, app)
        } catch (e: Exception) {
            Log.w(TAG, "Yandex MapKit not found")
        }
    }
}
