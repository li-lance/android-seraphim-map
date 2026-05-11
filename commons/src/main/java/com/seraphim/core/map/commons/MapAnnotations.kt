package com.seraphim.core.map.commons

import com.seraphim.core.map.commons.model.CircleOptions
import com.seraphim.core.map.commons.model.Marker
import com.seraphim.core.map.commons.model.MarkerOptions
import com.seraphim.core.map.commons.model.PolygonOptions
import com.seraphim.core.map.commons.model.PolylineOptions

/**
 * Marker and shape management.
 */
interface MapAnnotations {
    /** Add a marker. Returns a handle for later removal. */
    fun addMarker(options: MarkerOptions): Marker

    /** Remove a specific marker. */
    fun removeMarker(marker: Marker)

    /** Remove a marker by its ID. */
    fun removeMarkerById(id: String)

    /** Remove all markers. */
    fun clearMarkers()

    /** Add a polyline. */
    fun addPolyline(options: PolylineOptions): com.seraphim.core.map.commons.model.Polyline

    /** Add a polygon. */
    fun addPolygon(options: PolygonOptions): com.seraphim.core.map.commons.model.Polygon

    /** Add a circle. */
    fun addCircle(options: CircleOptions): com.seraphim.core.map.commons.model.Circle

    /** Remove all shapes (polylines, polygons, circles) but keep markers. */
    fun clearShapes()

    /** Remove all markers and shapes. */
    fun clearAll()
}
