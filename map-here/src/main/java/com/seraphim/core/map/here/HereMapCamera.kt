package com.seraphim.core.map.here

import android.graphics.Point
import com.here.sdk.core.Angle
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapView
import com.seraphim.core.map.commons.model.CameraPosition
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.model.LatLngBounds
import com.seraphim.core.map.commons.MapCamera as IMapCamera

/**
 * [IMapCamera] implementation for HERE SDK.
 */
class HereMapCamera(
    private val mapViewProvider: () -> MapView?
) : IMapCamera {

    private val mapView: MapView
        get() = mapViewProvider()
            ?: throw IllegalStateException("MapView is not available. Has init() been called?")

    private val camera: MapCamera
        get() = mapView.camera

    override val current: CameraPosition
        get() {
            val state = camera.state
            return CameraPosition(
                target = LatLng(
                    state.targetCoordinates.latitude,
                    state.targetCoordinates.longitude
                ),
                zoom = state.zoomLevel.toFloat(),
                tilt = state.tilt,
                bearing = state.orientation
            )
        }

    override val visibleRegion: LatLngBounds
        get() {
            val geoBox = mapView.getVisibleGeoBox() ?: GeoBox(
                GeoCoordinates(0.0, 0.0),
                GeoCoordinates(0.0, 0.0)
            )
            return LatLngBounds(
                southwest = LatLng(
                    geoBox.southWestCorner.latitude,
                    geoBox.southWestCorner.longitude
                ),
                northeast = LatLng(
                    geoBox.northEastCorner.latitude,
                    geoBox.northEastCorner.longitude
                )
            )
        }

    override fun moveTo(target: LatLng, zoom: Float?) {
        val coords = GeoCoordinates(target.latitude, target.longitude)
        if (zoom != null) {
            camera.lookAt(coords, MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom.toDouble()))
        } else {
            camera.lookAt(coords)
        }
    }

    override fun animateTo(
        target: LatLng,
        zoom: Float?,
        tilt: Float?,
        bearing: Float?,
        durationMs: Int
    ) {
        val coords = GeoCoordinates(target.latitude, target.longitude)
        val currentZoom = camera.state.zoomLevel.toFloat()
        val animation = MapCameraAnimationFactory.flyTo(
            coords,
            MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, (zoom ?: currentZoom).toDouble()),
            Angle(tilt ?: camera.state.tilt),
            Angle(bearing ?: camera.state.orientation),
            durationMs
        )
        camera.startAnimation(animation)
    }

    override fun animateToBounds(
        bounds: LatLngBounds,
        paddingPx: Int,
        durationMs: Int,
        onFinish: ((canceled: Boolean) -> Unit)?
    ) {
        val geoBox = GeoBox(
            GeoCoordinates(bounds.southwest.latitude, bounds.southwest.longitude),
            GeoCoordinates(bounds.northeast.latitude, bounds.northeast.longitude)
        )
        val animation = MapCameraAnimationFactory.flyTo(geoBox, durationMs)
        camera.startAnimation(animation) {
            onFinish?.invoke(false)
        }
    }

    override fun zoomIn() {
        camera.zoomBy(1.0, 300)
    }

    override fun zoomOut() {
        camera.zoomBy(-1.0, 300)
    }

    override fun zoomBy(amount: Float) {
        camera.zoomBy(amount.toDouble(), 300)
    }

    override fun screenToLatLng(x: Int, y: Int): LatLng {
        val coords = mapView.viewToGeoCoordinates(Point2D(x.toDouble(), y.toDouble()))
            ?: GeoCoordinates(0.0, 0.0)
        return LatLng(coords.latitude, coords.longitude)
    }

    override fun latLngToScreen(location: LatLng): Point {
        val point = mapView.geoToViewCoordinates(
            GeoCoordinates(location.latitude, location.longitude)
        ) ?: Point2D(0.0, 0.0)
        return Point(point.x.toInt(), point.y.toInt())
    }
}
