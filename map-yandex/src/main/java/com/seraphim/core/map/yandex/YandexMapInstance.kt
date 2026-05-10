package com.seraphim.core.map.yandex

import android.graphics.Bitmap
import android.util.Log
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapStyle
import com.seraphim.core.map.commons.MapUiSettings
import com.seraphim.core.map.commons.model.CameraState
import com.seraphim.core.map.commons.model.ClusterItem
import com.seraphim.core.map.commons.model.IconProvider
import com.seraphim.core.map.commons.model.MapType
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.seraphim.core.map.commons.model.Circle as ModelCircle
import com.seraphim.core.map.commons.model.CircleOptions as ModelCircleOptions
import com.seraphim.core.map.commons.model.LatLng as ModelLatLng
import com.seraphim.core.map.commons.model.Marker as ModelMarker
import com.seraphim.core.map.commons.model.MarkerOptions as ModelMarkerOptions
import com.seraphim.core.map.commons.model.Polygon as ModelPolygon
import com.seraphim.core.map.commons.model.PolygonOptions as ModelPolygonOptions
import com.seraphim.core.map.commons.model.Polyline as ModelPolyline
import com.seraphim.core.map.commons.model.PolylineOptions as ModelPolylineOptions

/**
 * [MapInstance] implementation for Yandex MapKit 4.33.1.
 */
open class YandexMapInstance : MapInstance {

    private var mapView: MapView? = null
    private val mv: MapView
        get() = mapView ?: throw IllegalStateException("MapView not initialized")

    private val mapObjectCollection: MapObjectCollection
        get() = mv.mapWindow.map.mapObjects

    private val markers = ConcurrentHashMap<String, PlacemarkMapObject>()
    private val polylines = mutableListOf<PolylineMapObject>()
    private val polygons = mutableListOf<PolygonMapObject>()
    private val circles = mutableListOf<CircleMapObject>()

    override val camera = YandexMapCamera { mapView }
    override val uiSettings: MapUiSettings = YandexMapUiSettings { mapView }

    override suspend fun init(host: MapHost, options: MapOptions) {
        val mv = host.awaitNativeMap() as? MapView
            ?: throw IllegalArgumentException("MapHost must provide a MapView")
        this.mapView = mv

        applyInitialCamera(options)
        applyMapType(options.mapType)
        applyUiSettings(options.uiSettings)
        applyStyle(options.style)
        setupListeners()
    }

    override suspend fun refreshHost(host: MapHost) {
        val mv = host.awaitNativeMap() as? MapView
            ?: throw IllegalArgumentException("MapHost must provide a MapView")
        this.mapView = mv
    }

    private fun applyInitialCamera(options: MapOptions) {
        when (val cam = options.initialCamera) {
            is com.seraphim.core.map.commons.InitialCamera.Position -> {
                mv.mapWindow.map.move(
                    CameraPosition(
                        Point(cam.target.latitude, cam.target.longitude),
                        cam.zoom, 0f, 0f
                    )
                )
            }

            is com.seraphim.core.map.commons.InitialCamera.Bounds -> {
                val center = Point(
                    (cam.bounds.southwest.latitude + cam.bounds.northeast.latitude) / 2.0,
                    (cam.bounds.southwest.longitude + cam.bounds.northeast.longitude) / 2.0
                )
                mv.mapWindow.map.move(CameraPosition(center, 15f, 0f, 0f))
            }

            is com.seraphim.core.map.commons.InitialCamera.None -> {}
        }
    }

    private fun applyMapType(type: MapType) {
        val mapType = when (type) {
            MapType.SATELLITE -> com.yandex.mapkit.map.MapType.SATELLITE
            MapType.HYBRID -> com.yandex.mapkit.map.MapType.HYBRID
            else -> com.yandex.mapkit.map.MapType.VECTOR_MAP
        }
        mv.mapWindow.map.mapType = mapType
    }

    private fun applyUiSettings(settings: com.seraphim.core.map.commons.UiSettings) {
        val map = mv.mapWindow.map
        map.isScrollGesturesEnabled = settings.scrollGesturesEnabled
        map.isZoomGesturesEnabled = settings.zoomGesturesEnabled
        map.isRotateGesturesEnabled = settings.rotateGesturesEnabled
        // isTiltGesturesEnabled not available in 4.33.1 separately
    }

    private fun applyStyle(style: MapStyle) {
        when (style) {
            is MapStyle.CustomJson -> {
                mv.mapWindow.map.setMapStyle(style.jsonString)
            }

            else -> {}
        }
    }

    private fun setupListeners() {}

    override fun addMarker(options: ModelMarkerOptions): ModelMarker {
        val point = Point(options.position.latitude, options.position.longitude)
        val imageProvider = createImageProvider(options.icon)
        val placemark = mapObjectCollection.addPlacemark(point, imageProvider)
        placemark.opacity = options.alpha
        placemark.isVisible = options.visible
        placemark.zIndex = options.zIndex

        val id = "yandex_" + UUID.randomUUID().toString().take(8)
        placemark.userData = id
        markers[id] = placemark
        return YandexMarker(id, placemark)
    }

    private fun createImageProvider(icon: IconProvider?): ImageProvider {
        return when (icon) {
            is IconProvider.FromBitmap -> {
                val bitmap = icon.bitmapDescriptor as? Bitmap
                    ?: return ImageProvider.fromResource(
                        mv.context,
                        android.R.drawable.ic_menu_mylocation
                    )
                ImageProvider.fromBitmap(bitmap)
            }

            else -> ImageProvider.fromResource(
                mv.context, android.R.drawable.ic_menu_mylocation
            )
        }
    }

    override fun removeMarker(marker: ModelMarker) {
        val ym = (marker as? YandexMarker)?.native ?: return
        mapObjectCollection.remove(ym)
        markers.values.remove(ym)
    }

    override fun removeMarkerById(id: String) {
        markers[id]?.let { mapObjectCollection.remove(it) }
        markers.remove(id)
    }

    override fun clearMarkers() {
        mapObjectCollection.clear()
        markers.clear()
    }

    override fun addPolyline(options: ModelPolylineOptions): ModelPolyline {
        val polyline = Polyline(
            options.points.map { Point(it.latitude, it.longitude) }
        )
        val obj = mapObjectCollection.addPolyline(polyline)
        obj.setStrokeColor(options.color)
        obj.strokeWidth = options.width
        obj.zIndex = options.zIndex
        obj.isVisible = options.visible
        polylines.add(obj)
        return YandexPolyline(obj)
    }

    override fun addPolygon(options: ModelPolygonOptions): ModelPolygon {
        val points = options.points.map { Point(it.latitude, it.longitude) }
        if (points.size < 3) {
            throw IllegalArgumentException("Polygon requires at least 3 points")
        }
        val polygon = Polygon(LinearRing(points), emptyList())
        val obj = mapObjectCollection.addPolygon(polygon)
        obj.fillColor = options.fillColor
        obj.strokeColor = options.strokeColor
        obj.strokeWidth = options.strokeWidth
        obj.zIndex = options.zIndex
        obj.isVisible = options.visible
        polygons.add(obj)
        return YandexPolygon(obj)
    }

    override fun addCircle(options: ModelCircleOptions): ModelCircle {
        val circle = Circle(
            Point(options.center.latitude, options.center.longitude),
            options.radius.toFloat()
        )
        val obj = mapObjectCollection.addCircle(circle)
        obj.fillColor = options.fillColor
        obj.strokeColor = options.strokeColor
        obj.strokeWidth = options.strokeWidth
        obj.zIndex = options.zIndex
        obj.isVisible = options.visible
        circles.add(obj)
        return YandexCircle(obj)
    }

    override fun clearShapes() {
        polylines.forEach { mapObjectCollection.remove(it) }
        polylines.clear()
        polygons.forEach { mapObjectCollection.remove(it) }
        polygons.clear()
        circles.forEach { mapObjectCollection.remove(it) }
        circles.clear()
    }

    override var mapType: MapType
        get() = when (mv.mapWindow.map.mapType) {
            com.yandex.mapkit.map.MapType.SATELLITE -> MapType.SATELLITE
            com.yandex.mapkit.map.MapType.HYBRID -> MapType.HYBRID
            else -> MapType.NORMAL
        }
        set(value) {
            applyMapType(value)
        }

    override fun enableUserLocation(enabled: Boolean) {
        Log.d(TAG, "enableUserLocation: $enabled")
    }

    override var onMapClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            mv.mapWindow.map.addInputListener(object : InputListener {
                override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                    value?.invoke(ModelLatLng(point.latitude, point.longitude))
                }

                override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {}
            })
        }

    override var onMapLongClick: ((ModelLatLng) -> Unit)? = null
        set(value) {
            field = value
            mv.mapWindow.map.addInputListener(object : InputListener {
                override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {}
                override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                    value?.invoke(ModelLatLng(point.latitude, point.longitude))
                }
            })
        }

    override var onMarkerClick: ((markerId: String) -> Boolean)? = null
        set(value) {
            field = value
            val listener = MapObjectTapListener { mapObject, _ ->
                val id = mapObject.userData as? String ?: return@MapObjectTapListener false
                value?.invoke(id) ?: false
            }
            markers.values.forEach { it.addTapListener(listener) }
        }

    override var onCameraChange: ((CameraState) -> Unit)? = null
        set(value) {
            field = value
            // TODO: CameraListener API differs in Yandex 4.33.1.
            // Verify the correct listener interface and method signature.
        }

    override fun setClusterItems(items: List<ClusterItem>) {
        throw UnsupportedOperationException(
            "Use YandexClusterableMap for clustering support."
        )
    }

    override fun clearAll() {
        clearMarkers()
        clearShapes()
    }

    companion object {
        private const val TAG = "YandexMapInstance"
    }
}

private class YandexMarker(
    override val id: String,
    val native: PlacemarkMapObject
) : ModelMarker {
    override var position: ModelLatLng
        get() = ModelLatLng(native.geometry.latitude, native.geometry.longitude)
        set(value) {
            native.geometry = Point(value.latitude, value.longitude)
        }
    override var visible: Boolean
        get() = native.isVisible
        set(value) {
            native.isVisible = value
        }

    override fun remove() {}
}

private class YandexPolyline(private val native: PolylineMapObject) : ModelPolyline {
    override fun remove() {}
}

private class YandexPolygon(private val native: PolygonMapObject) : ModelPolygon {
    override fun remove() {}
}

private class YandexCircle(private val native: CircleMapObject) : ModelCircle {
    override fun remove() {}
}
