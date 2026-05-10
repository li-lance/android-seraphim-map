package com.seraphim.core.map.commons

import androidx.annotation.RawRes

/**
 * Map styling configuration.
 */
sealed class MapStyle {
    /** Default provider map style. */
    object Default : MapStyle()

    /** Custom style defined by a JSON string (Google Maps, Yandex). */
    data class CustomJson(val jsonString: String) : MapStyle()

    /** Custom style loaded from a raw resource file. */
    data class FromResource(@RawRes val resId: Int) : MapStyle()
}
