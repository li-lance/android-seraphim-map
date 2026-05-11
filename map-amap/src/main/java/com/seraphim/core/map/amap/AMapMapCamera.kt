package com.seraphim.core.map.amap

import android.graphics.Point
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.amap.api.maps.model.LatLng as AMapLatLng
import com.amap.api.maps.model.LatLngBounds as AMapLatLngBounds
import com.seraphim.core.map.commons.model.CameraPosition as ModelCameraPosition

class AMapMapCamera(private val am: () -> AMap?) : MapCamera {
    private val m: AMap get() = am() ?: throw IllegalStateException()

    override val current: ModelCameraPosition
        get() {
            val p = m.cameraPosition
            return ModelCameraPosition(
                LatLng(p.target.latitude, p.target.longitude),
                p.zoom,
                p.tilt,
                p.bearing
            )
        }

    override val visibleRegion: LatLngBounds
        get() {
            val r = m.projection.visibleRegion
            return LatLngBounds(
                LatLng(r.latLngBounds.southwest.latitude, r.latLngBounds.southwest.longitude),
                LatLng(r.latLngBounds.northeast.latitude, r.latLngBounds.northeast.longitude)
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        m.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                AMapLatLng(
                    target.latitude,
                    target.longitude
                ), zoom ?: m.cameraPosition.zoom
            )
        )
    }

    override fun moveTo(position: ModelCameraPosition) {
        m.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                    .target(AMapLatLng(position.target.latitude, position.target.longitude))
                    .zoom(position.zoom)
                    .tilt(position.tilt)
                    .bearing(position.bearing)
                    .build()
            )
        )
    }

    override fun animateTo(
        target: LatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        val b = CameraPosition.builder()
            .target(AMapLatLng(target.latitude, target.longitude))
            .zoom(zoom ?: m.cameraPosition.zoom)
            .tilt(tilt ?: m.cameraPosition.tilt)
            .bearing(bearing ?: m.cameraPosition.bearing)
            .build()
        m.animateCamera(CameraUpdateFactory.newCameraPosition(b), durationMs.toLong(), null)
    }

    override fun animateTo(position: ModelCameraPosition, durationMs: Int) {
        val b = CameraPosition.builder()
            .target(AMapLatLng(position.target.latitude, position.target.longitude))
            .zoom(position.zoom)
            .tilt(position.tilt)
            .bearing(position.bearing)
            .build()
        m.animateCamera(CameraUpdateFactory.newCameraPosition(b), durationMs.toLong(), null)
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((Boolean) -> Unit)?
    ) {
        val b = AMapLatLngBounds.builder()
            .include(AMapLatLng(bounds.southwest.latitude, bounds.southwest.longitude))
            .include(AMapLatLng(bounds.northeast.latitude, bounds.northeast.longitude))
            .build()
        m.animateCamera(
            CameraUpdateFactory.newLatLngBounds(b, paddingPx),
            durationMs.toLong(),
            object : AMap.CancelableCallback {
                override fun onFinish() = onFinish?.invoke(false) ?: Unit
                override fun onCancel() = onFinish?.invoke(true) ?: Unit
            })
    }

    override fun zoomIn() {
        m.animateCamera(CameraUpdateFactory.zoomIn())
    }

    override fun zoomOut() {
        m.animateCamera(CameraUpdateFactory.zoomOut())
    }

    override fun zoomBy(amount: Float) {
        m.animateCamera(CameraUpdateFactory.zoomBy(amount))
    }

    // ── Coordinate Conversion ──

    override fun screenToLatLng(x: Int, y: Int): LatLng {
        val p = m.projection.fromScreenLocation(Point(x, y))
        return LatLng(p.latitude, p.longitude)
    }

    override fun latLngToScreen(loc: LatLng): Point =
        m.projection.toScreenLocation(AMapLatLng(loc.latitude, loc.longitude))
}
