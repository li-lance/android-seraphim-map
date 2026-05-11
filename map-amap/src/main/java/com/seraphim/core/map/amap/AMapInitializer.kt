package com.seraphim.core.map.amap

import android.app.Application
import com.amap.api.maps.MapsInitializer

/**
 * AMap SDK initializer. Call from [Application.onCreate] to handle
 * privacy compliance and global configuration.
 *
 * Usage:
 * ```
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         AMapInitializer.init(this)
 *     }
 * }
 * ```
 *
 * Or inline without extending Application:
 * ```
 * AMapInitializer.init(context)
 * ```
 */
object AMapInitializer {

    private var initialized = false

    /**
     * Initialize AMap SDK with privacy compliance and global settings.
     * Call once from Application.onCreate().
     *
     * @param app The Application context.
     * @param agreePrivacy Whether the user has agreed to privacy policy (default: true).
     * @param containPrivacy Whether the app contains a privacy policy page (default: true).
     */
    fun init(app: Application, agreePrivacy: Boolean = true, containPrivacy: Boolean = true) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            MapsInitializer.updatePrivacyShow(app, true, containPrivacy)
            MapsInitializer.updatePrivacyAgree(app, agreePrivacy)
            initialized = true
        }
    }
}
