package com.seraphim.core.map.tmap

import android.graphics.Point
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.skt.Tmap.TMapView

class TmapMapCamera(
    private val mapViewProvider: () -> TMapView?
) : MapCamera {

    private val mapView: TMapView
        get() = mapViewProvider() ?: throw IllegalStateException("TMapView not available")

    override val current: CameraPosition
        get() {
            val center = mapView.getCenterPoint()
            return CameraPosition(
                target = LatLng(center.latitude, center.longitude),
                zoom = mapView.zoomLevel.toFloat()
            )
        }

    override val visibleRegion: LatLngBounds
        get() {
            // Tmap doesn't expose visible region directly
            val c = mapView.getCenterPoint()
            return LatLngBounds(
                southwest = LatLng(c.latitude - 0.01, c.longitude - 0.01),
                northeast = LatLng(c.latitude + 0.01, c.longitude + 0.01)
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        mapView.setCenterPoint(target.longitude, target.latitude)
        if (zoom != null) mapView.zoomLevel = zoom.toInt()
    }

    override fun animateTo(
        target: LatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        mapView.setCenterPoint(target.longitude, target.latitude)
        if (zoom != null) mapView.zoomLevel = zoom.toInt()
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((Boolean) -> Unit)?
    ) {
        val cLat = (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0
        val cLng = (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
        mapView.setCenterPoint(cLng, cLat)
        onFinish?.invoke(false)
    }

    override fun zoomIn() {
        mapView.MapZoomIn()
    }

    override fun zoomOut() {
        mapView.MapZoomOut()
    }

    override fun zoomBy(amount: Float) {
        mapView.zoomLevel = (mapView.zoomLevel + amount.toInt()).coerceIn(1, 19)
    }

    override fun screenToLatLng(x: Int, y: Int): LatLng {
        // TODO: Tmap screen-to-geo conversion
        return LatLng(0.0, 0.0)
    }

    override fun latLngToScreen(location: LatLng): Point {
        // TODO: Tmap geo-to-screen conversion
        return Point(0, 0)
    }
}
