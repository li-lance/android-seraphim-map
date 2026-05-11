package com.seraphim.core.map.commons

import android.app.Application
import android.util.Log
import com.seraphim.core.map.commons.MapInitializer.init
import com.seraphim.core.map.commons.registry.MapProviderRegistry

/**
 * Provider SDK initialization. Call from [Application.onCreate].
 *
 * Usage:
 * ```
 * MapInitializer.init(this, MapProviders.AMAP)
 * MapInitializer.init(this, MapProviders.YANDEX, MapCredentials.ApiKey("your_key"))
 * MapInitializer.init(this, MapProviders.HERE, MapCredentials.ApiKeySecret("id", "secret"))
 * ```
 */
object MapInitializer {
    private const val TAG = "MapInitializer"

    /** The active provider ID, set during [init]. */
    var activeProvider: String = ""
        private set

    /**
     * Initialize the provider SDK via its registered [MapSdkInitializer].
     *
     * @param app The application context.
     * @param providerId The provider identifier (see [MapProviders]).
     * @param credentials Optional credentials for SDK initialization.
     */
    fun init(
        app: Application,
        providerId: String,
        credentials: MapCredentials = MapCredentials.None
    ) {
        activeProvider = providerId
        val factory = try {
            MapProviderRegistry.instance.get(providerId)
        } catch (e: NoSuchElementException) {
            Log.w(TAG, "No factory registered for '$providerId'. Did you call register()?")
            return
        }
        if (factory is MapSdkInitializer) {
            factory.init(app, credentials)
        } else {
            Log.d(TAG, "$providerId: no SDK init required")
        }
    }
}
