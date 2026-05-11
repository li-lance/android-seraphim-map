package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.ClusterItem

/**
 * Core map instance interface.
 *
 * Combines [MapViewport], [MapAnnotations], [MapEvents], and [MapLocation]
 * into a single facade. Provider implementations extend this interface.
 *
 * Lifecycle: Call [init] to start, then use the map. Call the [MapHost]'s
 * lifecycle methods as appropriate for your Android lifecycle.
 */
interface MapInstance : MapViewport, MapAnnotations, MapEvents, MapLocation {

    /** The camera controller (fine-grained animation). */
    val camera: MapCamera

    /** UI settings snapshot. Use [updateUiSettings] to apply changes. */
    val uiSettings: UiSettings

    /** Apply new UI settings to the map. */
    fun updateUiSettings(settings: UiSettings)

    /**
     * Initialize the map with the given host and options.
     * Must be called exactly once.
     */
    suspend fun init(host: MapHost, options: MapOptions)

    /**
     * Refresh the map host reference, e.g., after Fragment recreation.
     */
    suspend fun refreshHost(host: MapHost)

    /**
     * Set cluster items on the map.
     * Providers without clustering support should throw [UnsupportedOperationException].
     * Use [ClusterableMap] for full clustering support.
     */
    @Throws(UnsupportedOperationException::class)
    fun setClusterItems(items: List<ClusterItem>)
}
