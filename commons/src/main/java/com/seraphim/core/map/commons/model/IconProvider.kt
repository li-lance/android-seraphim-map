package com.seraphim.core.map.commons.model

import androidx.annotation.DrawableRes

/**
 * Abstraction for providing marker icons from various sources.
 */
sealed class IconProvider {
    /** Icon from an Android drawable resource. */
    data class FromDrawable(@DrawableRes val resId: Int) : IconProvider()

    /** Icon from a bitmap (provider-specific descriptor). */
    data class FromBitmap(val bitmapDescriptor: Any) : IconProvider()

    /** Icon from an asset file path. */
    data class FromAsset(val assetPath: String) : IconProvider()

    /** Default provider icon (typically a blue pin). */
    object Default : IconProvider()
}
