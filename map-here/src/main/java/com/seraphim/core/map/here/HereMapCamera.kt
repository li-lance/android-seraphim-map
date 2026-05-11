package com.seraphim.core.map.here

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.MapCamera
import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.here.sdk.mapview.MapCamera as HereMapNativeCamera

class HereMapCamera(private val mv: MapView) : MapCamera {
    private val camera: HereMapNativeCamera get() = mv.camera

    override val current: CameraPosition
        get() {
            val s = camera.state
            return CameraPosition(
                LatLng(
                    s.targetCoordinates.latitude,
                    s.targetCoordinates.longitude
                ), s.zoomLevel.toFloat()
            )
        }

    override val visibleRegion: LatLngBounds
        get() {
            val sw = mv.viewToGeoCoordinates(Point2D(0.0, mv.height.toDouble()))!!
            val ne = mv.viewToGeoCoordinates(Point2D(mv.width.toDouble(), 0.0))!!
            return LatLngBounds(
                LatLng(sw.latitude, sw.longitude),
                LatLng(ne.latitude, ne.longitude)
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        camera.lookAt(GeoCoordinates(target.latitude, target.longitude))
        if (zoom != null) camera.zoomTo(zoom.toDouble())
    }

    override fun animateTo(
        target: LatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        moveTo(target, zoom)
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((Boolean) -> Unit)?
    ) {
        moveTo(
            LatLng(
                (bounds.southwest.latitude + bounds.northeast.latitude) / 2,
                (bounds.southwest.longitude + bounds.northeast.longitude) / 2
            )
        )
        onFinish?.invoke(false)
    }

    override fun zoomIn() { /* HERE 4.25.5: camera.zoomIn() or zoomBy */
    }

    override fun zoomOut() { /* HERE 4.25.5: camera.zoomOut() or zoomBy */
    }

    override fun zoomBy(amount: Float) { /* TODO */
    }

    override fun screenToLatLng(x: Int, y: Int): LatLng {
        val g = mv.viewToGeoCoordinates(Point2D(x.toDouble(), y.toDouble()))!!
        return LatLng(g.latitude, g.longitude)
    }

    override fun latLngToScreen(loc: LatLng): android.graphics.Point {
        val p = mv.geoToViewCoordinates(GeoCoordinates(loc.latitude, loc.longitude))!!
        return android.graphics.Point(p.x.toInt(), p.y.toInt())
    }
}
