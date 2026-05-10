package com.seraphim.core.map.google

import android.graphics.Point
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.CameraPosition as ModelCameraPosition
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng
import com.seraphim.core.map.commons.model.LatLngBounds as ModelLatLngBounds

/**
 * [MapCamera] implementation for Google Maps.
 */
class GoogleMapCamera(
    private val mapProvider: () -> GoogleMap?
) : MapCamera {

    private val map: GoogleMap
        get() = mapProvider()
            ?: throw IllegalStateException("GoogleMap is not available. Has init() been called?")

    override val current: ModelCameraPosition
        get() {
            val pos = map.cameraPosition
            return ModelCameraPosition(
                target = pos.target.toModel(),
                zoom = pos.zoom,
                tilt = pos.tilt,
                bearing = pos.bearing
            )
        }

    override val visibleRegion: ModelLatLngBounds
        get() {
            val region = map.projection.visibleRegion
            return ModelLatLngBounds(
                southwest = ModelLatLng(region.nearLeft.latitude, region.nearLeft.longitude),
                northeast = ModelLatLng(region.farRight.latitude, region.farRight.longitude)
            )
        }

    override fun moveTo(target: ModelLatLng, zoom: Float?) {
        val latLng = LatLng(target.latitude, target.longitude)
        if (zoom != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun animateTo(
        target: ModelLatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        val latLng = LatLng(target.latitude, target.longitude)
        val current = map.cameraPosition
        val builder = CameraPosition.builder()
            .target(latLng)
            .zoom(zoom ?: current.zoom)
            .tilt(tilt ?: current.tilt)
            .bearing(bearing ?: current.bearing)

        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(builder.build()),
            durationMs,
            null
        )
    }

    override fun animateToBounds(
        bounds: ModelLatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((canceled: Boolean) -> Unit)?
    ) {
        val latLngBounds = LatLngBounds.builder()
            .include(LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
            .include(LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
            .build()

        val callback = if (onFinish != null) {
            object : GoogleMap.CancelableCallback {
                override fun onFinish() = onFinish(false)
                override fun onCancel() = onFinish(true)
            }
        } else null

        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, paddingPx),
            durationMs,
            callback
        )
    }

    override fun zoomIn() {
        map.animateCamera(CameraUpdateFactory.zoomIn())
    }

    override fun zoomOut() {
        map.animateCamera(CameraUpdateFactory.zoomOut())
    }

    override fun zoomBy(amount: Float) {
        map.animateCamera(CameraUpdateFactory.zoomBy(amount))
    }

    override fun screenToLatLng(x: Int, y: Int): ModelLatLng {
        val latLng = map.projection.fromScreenLocation(Point(x, y))
        return ModelLatLng(latLng.latitude, latLng.longitude)
    }

    override fun latLngToScreen(location: ModelLatLng): Point {
        return map.projection.toScreenLocation(LatLng(location.latitude, location.longitude))
    }

    private fun LatLng.toModel() = ModelLatLng(latitude, longitude)
}
