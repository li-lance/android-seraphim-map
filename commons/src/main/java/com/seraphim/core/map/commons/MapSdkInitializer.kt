package com.seraphim.core.map.commons

import android.app.Application

/**
 * SDK initialization for a map provider.
 *
 * Implemented by [MapInstanceFactory] when a provider requires
 * global SDK initialization (e.g., privacy compliance, API keys).
 *
 * Providers without SDK-level init (e.g., Google Maps) can use
 * the default no-op implementation.
 */
interface MapSdkInitializer {
    /** Unique provider ID, must match [MapInstanceFactory.providerId]. */
    val providerId: String

    /**
     * Initialize the provider SDK. Called once from Application.onCreate.
     *
     * @param app The application context.
     * @param credentials Optional credentials (API key, key+secret, etc.).
     */
    fun init(app: Application, credentials: MapCredentials = MapCredentials.None)
}
