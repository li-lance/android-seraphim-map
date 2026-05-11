package com.seraphim.core.map.commons

import android.app.Application
import android.util.Log
import com.seraphim.core.map.commons.MapInitializer.init

/**
 * Provider SDK initialization. Call from [Application.onCreate].
 *
 * Usage:
 * ```
 * // Single-key providers
 * MapInitializer.init(this, MapProviders.AMAP)
 * MapInitializer.init(this, MapProviders.YANDEX, apiKey = "your_key")
 *
 * // Two-key providers (HERE)
 * MapInitializer.init(this, MapProviders.HERE, apiKey = "keyId", apiSecret = "keySecret")
 * ```
 */
object MapInitializer {
    private const val TAG = "MapInitializer"

    /** The active provider ID, set during [init]. */
    var activeProvider: String = ""
        private set

    /** Single-key init. */
    fun init(app: Application, providerId: String, apiKey: String? = null) {
        init(app, providerId, apiKey, null)
    }

    /** Two-key init (HERE). */
    fun init(app: Application, providerId: String, apiKey: String?, apiSecret: String?) {
        activeProvider = providerId
        when (providerId) {
            MapProviders.AMAP -> initAMap(app)
            MapProviders.YANDEX -> initYandex(app, apiKey)
            MapProviders.HERE -> initHere(app, apiKey, apiSecret)
            MapProviders.GOOGLE -> Log.d(TAG, "Google: ensure API key in AndroidManifest")
            else -> Log.d(TAG, "$providerId: init via provider SDK")
        }
    }

    private fun initAMap(app: Application) {
        try {
            val mc = Class.forName("com.amap.api.maps.MapsInitializer")
            mc.getMethod(
                "updatePrivacyShow",
                android.content.Context::class.java,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            )
                .invoke(null, app, true, true)
            mc.getMethod(
                "updatePrivacyAgree",
                android.content.Context::class.java,
                Boolean::class.javaPrimitiveType
            )
                .invoke(null, app, true)

            try {
                val lc = Class.forName("com.amap.api.location.AMapLocationClient")
                lc.getMethod(
                    "updatePrivacyShow",
                    android.content.Context::class.java,
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType
                )
                    .invoke(null, app, true, true)
                lc.getMethod(
                    "updatePrivacyAgree",
                    android.content.Context::class.java,
                    Boolean::class.javaPrimitiveType
                )
                    .invoke(null, app, true)
            } catch (e: Exception) {
                Log.w(TAG, "AMap Location SDK not found, skip location privacy init")
            }
            Log.d(TAG, "AMap SDK initialized")
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

    private fun initHere(app: Application, accessKeyId: String?, accessKeySecret: String?) {
        if (accessKeyId == null || accessKeySecret == null) {
            Log.w(TAG, "HERE SDK requires accessKeyId + accessKeySecret")
            return
        }
        try {
            val authMode = Class.forName("com.here.sdk.core.engine.AuthenticationMode")
            val withKeySecret =
                authMode.getMethod("withKeySecret", String::class.java, String::class.java)
            val auth = withKeySecret.invoke(null, accessKeyId, accessKeySecret)

            val sdkOptions = Class.forName("com.here.sdk.core.engine.SDKOptions")
            val options = sdkOptions.getConstructor(authMode).newInstance(auth)

            val engineClass = Class.forName("com.here.sdk.core.engine.SDKNativeEngine")
            engineClass.getMethod(
                "makeSharedInstance",
                android.content.Context::class.java,
                sdkOptions
            )
                .invoke(null, app, options)
            Log.d(TAG, "HERE SDK initialized")
        } catch (e: Exception) {
            Log.w(TAG, "HERE SDK init failed: ${e.message}")
        }
    }
}
