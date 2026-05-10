package com.seraphim.core.map.commons.location

import com.seraphim.core.map.commons.model.UserPosition

/**
 * Result of a location request.
 */
sealed class LocationResult {
    /** Location successfully retrieved. */
    data class Success(val position: UserPosition) : LocationResult()

    /** Location permission denied by the user. */
    object PermissionDenied : LocationResult()

    /** Location is disabled in device settings. */
    object LocationDisabled : LocationResult()

    /** Location request timed out. */
    object Timeout : LocationResult()
}
