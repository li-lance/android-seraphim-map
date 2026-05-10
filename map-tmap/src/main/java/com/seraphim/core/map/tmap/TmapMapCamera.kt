package com.seraphim.core.map.tmap

import android.graphics.Point
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.skt.tmap.TMapView

class TmapMapCamera(private val mv: () -> TMapView?) : MapCamera {
    private val m: TMapView get() = mv() ?: throw IllegalStateException()

    override val current: CameraPosition
        get() {
            val c = m.getCenterPoint()
            return CameraPosition(LatLng(c.latitude, c.longitude), m.zoomLevel.toFloat())
        }

    override val visibleRegion: LatLngBounds
        get() {
            val c = m.getCenterPoint()
            val d = 0.01
            return LatLngBounds(
                LatLng(c.latitude - d, c.longitude - d),
                LatLng(c.latitude + d, c.longitude + d)
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        m.setCenterPoint(target.longitude, target.latitude)
        if (zoom != null) m.zoomLevel = zoom.toInt()
    }

    override fun animateTo(
        target: LatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        m.setCenterPoint(target.longitude, target.latitude)
        if (zoom != null) m.zoomLevel = zoom.toInt()
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((Boolean) -> Unit)?
    ) {
        val cl = (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0
        val cn = (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
        m.setCenterPoint(cn, cl)
        onFinish?.invoke(false)
    }

    override fun zoomIn() {
        m.mapZoomIn()
    }

    override fun zoomOut() {
        m.mapZoomOut()
    }

    override fun zoomBy(amount: Float) {
        m.zoomLevel = (m.zoomLevel + amount.toInt()).coerceIn(1, 19)
    }

    override fun screenToLatLng(x: Int, y: Int): LatLng = LatLng(0.0, 0.0)
    override fun latLngToScreen(location: LatLng): Point = Point(0, 0)
}
