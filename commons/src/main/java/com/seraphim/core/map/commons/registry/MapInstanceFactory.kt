package com.seraphim.core.map.commons.registry

import android.content.Context
import android.view.ViewGroup
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.search.PoiSearch

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
     * Create a [MapHost] without a parent container.
     * Providers that need a ViewGroup should throw [UnsupportedOperationException].
     */
    fun createMapHost(context: Context): MapHost

    /**
     * Create a [MapHost] attached to the given [parent] [ViewGroup].
     * Default implementation calls [createMapHost] without parent.
     */
    fun createMapHost(context: Context, parent: ViewGroup): MapHost = createMapHost(context)

    /**
     * Create a [MapInstance].
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

    /**
     * Create a [PoiSearch] for this map provider.
     */
    fun createPoiSearch(context: Context): PoiSearch
}
