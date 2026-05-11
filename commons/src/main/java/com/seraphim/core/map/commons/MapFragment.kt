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
import kotlinx.coroutines.launch

/**
 * Provider-agnostic map Fragment. Manages [MapHost] lifecycle and supports
 * provider switching at runtime.
 *
 * Usage:
 * ```
 * val fragment = MapFragment().apply { initialize(registry, "google") }
 * ```
 *
 * Switch provider: `fragment.switchProvider("yandex")`
 */
open class MapFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var container: FrameLayout? = null
    private var mapHost: MapHost? = null
    private var _map: MapInstance? = null
    private var _registry: MapProviderRegistry? = null
    private var _providerId: String? = null

    val map: MapInstance? get() = _map

    /** Suspend until the map is ready. */
    suspend fun getMap(): MapInstance {
        var attempts = 0
        while (_map == null && attempts < 100) {
            kotlinx.coroutines.delay(100)
            attempts++
        }
        return _map
            ?: throw IllegalStateException("Map not initialized. Call initialize() before adding Fragment.")
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
                    _map = factory.createMapInstance(requireContext(), MapOptions()).also {
                        it.init(host, MapOptions())
                    }
                }

                is MapAvailability.Unavailable -> {
                    android.util.Log.e(TAG, "Provider '$id' unavailable")
                }
            }
        }
    }

    private fun destroyMap() {
        _map = null; container?.removeAllViews(); mapHost?.onDestroy(); mapHost = null
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }
}
