package com.seraphim.core.map.amap

import android.content.Context
import android.location.Geocoder
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AMapLocationDecoder(private val ctx: Context) : LocationDecoder {

    override suspend fun reverseGeocode(req: GeocodingRequest): GeocodingResult {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(ctx)
                val addresses = geocoder.getFromLocation(
                    req.location.latitude,
                    req.location.longitude,
                    req.maxResults
                )
                val results = addresses?.map {
                    GeocodingAddress(it.latitude, it.longitude, it.getAddressLine(0) ?: "")
                } ?: emptyList()
                GeocodingResult(results.firstOrNull()?.formattedAddress, results)
            } catch (e: Exception) {
                GeocodingResult(null, emptyList())
            }
        }
    }

    override suspend fun forwardGeocode(
        query: String,
        language: String,
        maxResults: Int
    ): List<GeocodingAddress> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(ctx)
                val addresses = geocoder.getFromLocationName(query, maxResults)
                addresses?.map {
                    GeocodingAddress(it.latitude, it.longitude, it.getAddressLine(0) ?: "")
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
