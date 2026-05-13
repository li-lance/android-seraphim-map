package com.seraphim.core.map.yandex

/**
 * Yandex MapKit location-specific configuration.
 *
 * @param intervalMs Desired interval between location updates.
 * @param useSatellite Whether to use satellite positioning.
 * @param autoPause Whether to automatically pause updates when the app is backgrounded.
 */
data class YandexLocationConfig(
    val intervalMs: Long = 5000,
    val useSatellite: Boolean = false,
    val autoPause: Boolean = true
)
