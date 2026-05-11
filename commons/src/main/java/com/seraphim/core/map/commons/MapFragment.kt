package com.seraphim.core.map.commons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapProviderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Provider-agnostic map Fragment. Manages [MapHost] lifecycle and supports
 * provider switching at runtime.
 *
 * Usage:
 * ```
 * val fragment = MapFragment().apply { initialize(registry, "google") }
 * fragment.mapFlow.collect { mapInstance ->
 *     // mapInstance is non-null when ready
 * }
 * ```
 *
 * Switch provider: `fragment.switchProvider("yandex")`
 */
open class MapFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var container: FrameLayout? = null
    private var mapHost: MapHost? = null
    private var _registry: MapProviderRegistry? = null
    private var _providerId: String? = null

    private val _mapFlow = MutableStateFlow<MapInstance?>(null)

    /**
     * A [StateFlow] that emits the current [MapInstance] state.
     *
     * - `null` — map is not yet initialized or was destroyed.
     * - non-null — map is ready for use.
     *
     * Collect this in your UI layer (e.g., `lifecycleScope`) to react
     * to initialization, provider switches, and lifecycle events.
     */
    val mapFlow: StateFlow<MapInstance?> = _mapFlow

    /** Convenience accessor for the current map instance (null if not ready). */
    val map: MapInstance? get() = _mapFlow.value

    /** Suspend until the map is ready, with an optional timeout. */
    suspend fun awaitMap(timeoutMs: Long = 10000): MapInstance {
        return kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            _mapFlow.filterNotNull().first()
        } ?: throw IllegalStateException(
            "Map not initialized within ${timeoutMs}ms. " +
                    "Call initialize() before adding Fragment."
        )
    }

    fun initialize(registry: MapProviderRegistry, providerId: String) {
        _registry = registry; _providerId = providerId
    }

    fun switchProvider(providerId: String) {
        _providerId = providerId; destroyMap(); createMap()
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        return FrameLayout(requireContext()).also { container = it }.apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState); createMap()
    }

    override fun onResume() {
        super.onResume(); mapHost?.onResume()
    }

    override fun onPause() {
        super.onPause(); mapHost?.onPause()
    }

    override fun onDestroyView() {
        destroyMap(); scope.cancel(); super.onDestroyView()
    }

    // ── Internal ──

    private fun createMap() {
        val r = _registry ?: return;
        val id = _providerId ?: return
        val parent = container ?: return
        val factory = r.get(id)

        scope.launch {
            when (factory.checkAvailability(requireContext())) {
                is MapAvailability.Available -> {
                    val host = factory.createMapHost(requireContext(), parent)
                    mapHost = host
                    val instance = factory.createMapInstance(requireContext(), MapOptions()).also {
                        it.init(host, MapOptions())
                    }
                    _mapFlow.value = instance
                }

                is MapAvailability.Unavailable -> {
                    android.util.Log.e(TAG, "Provider '$id' unavailable")
                    _mapFlow.value = null
                }
            }
        }
    }

    private fun destroyMap() {
        _mapFlow.value = null
        container?.removeAllViews()
        mapHost?.onDestroy()
        mapHost = null
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
