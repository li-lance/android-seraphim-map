package com.seraphim.core.map.commons.model

/**
 * Represents the state of the map camera.
 */
sealed class CameraState {
    /** The camera is idle (not moving). */
    data class Idle(val position: CameraPosition) : CameraState()

    /** The camera is currently moving. */
    data class Moving(val position: CameraPosition) : CameraState()

    /** The camera has started moving, with a reason. */
    data class Started(val reason: CameraMoveReason) : CameraState()
}

/**
 * Reason for camera movement.
 */
enum class CameraMoveReason {
    /** User gesture (e.g., swipe, pinch). */
    GESTURE,

    /** API-triggered animation (e.g., animateTo). */
    API_ANIMATION,

    /** API-triggered direct move (e.g., moveTo). */
    API_DIRECT
}
