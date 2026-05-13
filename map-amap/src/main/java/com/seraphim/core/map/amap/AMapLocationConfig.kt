package com.seraphim.core.map.amap

/**
 * AMap location SDK-specific configuration.
 *
 * @param intervalMs Desired interval between location updates.
 * @param locationMode GPS / network / combined mode.
 * @param onceLocation Whether to stop after receiving the first location.
 * @param needAddress Whether to include reverse-geocoded address in the result.
 * @param cacheEnabled Whether to use cached location when available.
 * @param mockEnable Whether to allow mock locations.
 */
data class AMapLocationConfig(
    val intervalMs: Long = 5000,
    val locationMode: AMapLocationMode = AMapLocationMode.Hight_Accuracy,
    val onceLocation: Boolean = false,
    val needAddress: Boolean = true,
    val cacheEnabled: Boolean = true,
    val mockEnable: Boolean = false
)

enum class AMapLocationMode {
    Hight_Accuracy,
    Battery_Saving,
    Device_Sensors
}
