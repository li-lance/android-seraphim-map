package com.seraphim.core.map.commons.registry

import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions

/**
 * Manages the current active map provider and provides a unified API
 * for creating map-related components.
 *
 * Supports DI frameworks (Koin, Hilt, Dagger) and manual instantiation.
 *
 * Usage:
 * ```
 * // Manual
 * val registry = MapProviderRegistry()
 * registry.register(GoogleMapInstanceFactory())
 * val manager = MapManager(registry)
 *
 * // Koin
 * val module = module {
 *     single { MapProviderRegistry().apply { register(get()) } }
 *     single { MapManager(get()) }
 * }
 * ```
 */
class MapManager(
    private val registry: MapProviderRegistry
) {
    /** The currently active factory. */
    var current: MapInstanceFactory = registry.getDefault()
        private set

    /**
     * Switch to a different map provider.
     * @throws NoSuchElementException if the provider is not registered.
     */
    fun switchTo(providerId: String) {
        current = registry.get(providerId)
    }

    /**
     * Convenience: create a MapInstance using the current provider.
     */
    fun createMapInstance(context: android.content.Context, options: MapOptions): MapInstance =
        current.createMapInstance(context, options)
}
