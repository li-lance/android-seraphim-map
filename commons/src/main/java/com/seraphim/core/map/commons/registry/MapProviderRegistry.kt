package com.seraphim.core.map.commons.registry

/**
 * Registry of available [MapInstanceFactory] implementations.
 *
 * Supports multiple instances (non-singleton) for flexibility in testing
 * and multi-module scenarios. Each instance is independently managed.
 *
 * The owner of a Registry instance is responsible for disposing it
 * when no longer needed to avoid memory leaks.
 */
class MapProviderRegistry {
    private val _factories = mutableListOf<MapInstanceFactory>()

    /** All registered factories. */
    val factories: List<MapInstanceFactory>
        get() = _factories.toList()

    /**
     * Register a factory.
     * @throws IllegalArgumentException if a factory with the same [MapInstanceFactory.providerId] is already registered.
     */
    fun register(factory: MapInstanceFactory) {
        require(_factories.none { it.providerId == factory.providerId }) {
            "A factory with providerId '${factory.providerId}' is already registered"
        }
        _factories.add(factory)
    }

    /**
     * Unregister a factory by provider ID.
     */
    fun unregister(providerId: String) {
        _factories.removeAll { it.providerId == providerId }
    }

    /**
     * Get a factory by provider ID.
     * @throws NoSuchElementException if not found.
     */
    fun get(providerId: String): MapInstanceFactory =
        _factories.first { it.providerId == providerId }

    /**
     * Get the first registered factory as default.
     * @throws NoSuchElementException if no factories are registered.
     */
    fun getDefault(): MapInstanceFactory =
        _factories.first()

    /**
     * Check if a provider is registered.
     */
    fun hasProvider(providerId: String): Boolean =
        _factories.any { it.providerId == providerId }

    /**
     * Dispose the registry. Clears all registered factories.
     * Call this when the registry instance is no longer needed.
     */
    fun dispose() {
        _factories.clear()
    }
}
