package com.seraphim.core.map.commons

/**
 * View container abstraction for the native map view.
 * Handles lifecycle management and provides access to the underlying map object.
 *
 * Created by each provider's MapInstanceFactory.
 */
interface MapHost {
    /**
     * Wait for the native map object to become available, then return it.
     * The returned type is provider-specific (e.g., GoogleMap, MapScene, Map).
     */
    suspend fun awaitNativeMap(): Any

    /** Update the padding offsets of the map view. */
    fun updatePadding(left: Int, top: Int, right: Int, bottom: Int)

    // ── Lifecycle ──

    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()
    fun onLowMemory()
}
