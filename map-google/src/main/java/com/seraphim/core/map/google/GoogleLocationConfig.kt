package com.seraphim.core.map.google

import com.google.android.gms.location.Priority

private const val SIX_SECONDS = 6 * 1000L

/**
 * Google FusedLocationProvider-specific configuration.
 *
 * @param intervalMs Desired interval between location updates.
 * @param priority Accuracy vs. power trade-off. Default is high accuracy.
 * @param fastestIntervalMs Fastest rate at which your app can handle updates.
 * @param smallestDisplacementMeters Minimum distance between updates.
 * @param waitForAccurateLocation Whether to wait for a more accurate fix.
 * @param maxWaitTimeMs Maximum time the system may batch updates.
 */
data class GoogleLocationConfig(
    val intervalMs: Long = SIX_SECONDS,
    val priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
    val fastestIntervalMs: Long = SIX_SECONDS / 2,
    val smallestDisplacementMeters: Float = 0f,
    val waitForAccurateLocation: Boolean = true,
    val maxWaitTimeMs: Long = SIX_SECONDS
)
