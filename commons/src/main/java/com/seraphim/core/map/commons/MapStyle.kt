package com.seraphim.core.map.commons

import androidx.annotation.RawRes
import com.seraphim.core.map.commons.model.MapType

/**
 * Map styling configuration, including map type.
 */
sealed class MapStyle {
    /** Default provider map style. */
    object Default : MapStyle()

    /** Custom style defined by a JSON string (Google Maps, Yandex). */
    data class CustomJson(val jsonString: String) : MapStyle()

    /** Custom style loaded from a raw resource file. */
    data class FromResource(@RawRes val resId: Int) : MapStyle()

    /** Specific map type (normal, satellite, hybrid, terrain). */
    data class Type(val mapType: MapType) : MapStyle()
}
