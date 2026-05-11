package com.seraphim.core.map.commons

/**
 * Standard provider identifiers for use with [MapProviderRegistry] and [MapFragment].
 *
 * Usage:
 * ```
 * registry.register(GoogleMapInstanceFactory())
 * mapFragment.initialize(registry, MapProviders.GOOGLE)
 * mapFragment.switchProvider(MapProviders.YANDEX)
 * ```
 */
object MapProviders {
    const val GOOGLE = "google"
    const val HERE = "here"
    const val YANDEX = "yandex"
    const val TMAP = "tmap"
    const val AMAP = "amap"
}
