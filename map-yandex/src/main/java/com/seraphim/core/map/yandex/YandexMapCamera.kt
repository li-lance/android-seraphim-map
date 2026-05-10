package com.seraphim.core.map.yandex

import android.graphics.Point
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.seraphim.core.map.commons.model.CameraPosition as ModelCameraPosition
import com.yandex.mapkit.geometry.Point as YandexPoint

class YandexMapCamera(
    private val mapViewProvider: () -> MapView?
) : MapCamera {

    private val mapView: MapView
        get() = mapViewProvider() ?: throw IllegalStateException("MapView not available")

    private val map get() = mapView.mapWindow.map

    override val current: ModelCameraPosition
        get() {
            val pos = map.cameraPosition
            return ModelCameraPosition(
                target = LatLng(pos.target.latitude, pos.target.longitude),
                zoom = pos.zoom, tilt = pos.tilt, bearing = pos.azimuth
            )
        }

    override val visibleRegion: LatLngBounds
        get() {
            val region = map.visibleRegion
            return LatLngBounds(
                southwest = LatLng(region.bottomLeft.latitude, region.bottomLeft.longitude),
                northeast = LatLng(region.topRight.latitude, region.topRight.longitude)
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        val z = zoom ?: map.cameraPosition.zoom
        map.move(
            CameraPosition(
                YandexPoint(target.latitude, target.longitude),
                z,
                0f,
                map.cameraPosition.azimuth
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
        val cur = map.cameraPosition
        map.move(
            CameraPosition(
                YandexPoint(target.latitude, target.longitude),
                zoom ?: cur.zoom,
                tilt ?: cur.tilt,
                bearing ?: cur.azimuth
            ),
            Animation(Animation.Type.SMOOTH, durationMs.toFloat() / 1000f), null
        )
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((Boolean) -> Unit)?
    ) {
        val c = YandexPoint(
            (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0,
            (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
        )
        map.move(
            CameraPosition(c, map.cameraPosition.zoom, 0f, 0f),
            Animation(Animation.Type.SMOOTH, durationMs.toFloat() / 1000f),
            null
        )
        onFinish?.invoke(false)
    }

    override fun zoomIn() {
        val p = map.cameraPosition
        map.move(
            CameraPosition(p.target, p.zoom + 1f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    override fun zoomOut() {
        val p = map.cameraPosition
        map.move(
            CameraPosition(p.target, p.zoom - 1f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    override fun zoomBy(amount: Float) {
        val p = map.cameraPosition
        map.move(
            CameraPosition(p.target, p.zoom + amount, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    override fun screenToLatLng(x: Int, y: Int): LatLng = LatLng(0.0, 0.0)
    override fun latLngToScreen(location: LatLng): Point = Point(0, 0)
}
