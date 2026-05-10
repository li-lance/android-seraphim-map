package com.seraphim.core.map.tmap

import android.content.Context
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TmapLocationDecoder(context: Context) : LocationDecoder {

    private val tmapData: TMapData = TMapData()

    override suspend fun reverseGeocode(request: GeocodingRequest): GeocodingResult {
        return suspendCancellableCoroutine { continuation ->
            val point = TMapPoint(request.location.latitude, request.location.longitude)
            tmapData.convertGpsToAddress(point.latitude, point.longitude) { address ->
                val result = GeocodingAddress(
                    latitude = request.location.latitude,
                    longitude = request.location.longitude,
                    formattedAddress = address ?: "Unknown"
                )
                continuation.resume(
                    GeocodingResult(
                        formattedAddress = result.formattedAddress,
                        results = listOf(result)
                    )
                )
            }
        }
    }

    override suspend fun forwardGeocode(
        query: String, language: String, maxResults: Int
    ): List<GeocodingAddress> {
        // TODO: Tmap forward geocoding via TMapData.findPathData or address search API
        return emptyList()
    }
}
