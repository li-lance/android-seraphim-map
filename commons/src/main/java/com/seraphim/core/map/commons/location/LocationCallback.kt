package com.seraphim.core.map.commons.location

/**
 * Callback for receiving location updates.
 */
fun interface LocationCallback {
    fun onLocationResult(result: LocationResult)
}
