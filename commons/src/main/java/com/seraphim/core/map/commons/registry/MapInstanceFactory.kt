package com.seraphim.core.map.commons.registry

import android.content.Context
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider

/**
 * Factory for creating map instances and related components for a specific provider.
 *
 * Each map provider module (Google, HERE, Yandex, Tmap) provides an implementation.
 *
 * Factories should be lightweight and stateless; actual SDK initialization
 * happens lazily when [createMapInstance] is called.
 */
interface MapInstanceFactory {
    /** Unique identifier for this provider (e.g., "google", "here", "yandex", "tmap"). */
    val providerId: String

    /**
     * Check if this provider is available on the device.
     * E.g., Google Maps requires Google Play Services.
     */
    suspend fun checkAvailability(context: Context): MapAvailability

    /**
     * Create a [MapHost] from an Android fragment or view.
     * The provider is responsible for:
     *  - Creating the correct native MapView/MapFragment
     *  - Managing its lifecycle
     *  - Exposing the native map via [MapHost.awaitNativeMap]
     */
    fun createMapHost(context: Context): MapHost

    /**
     * Create a [MapInstance] initialized with the given host and options.
     */
    fun createMapInstance(context: Context, options: MapOptions): MapInstance

    /**
     * Create a [UserLocationProvider] for this map provider.
     */
    fun createUserLocationProvider(context: Context): UserLocationProvider

    /**
     * Create a [LocationDecoder] for this map provider.
     */
    fun createLocationDecoder(context: Context): LocationDecoder
}
