package com.seraphim.core.map.yandex

import android.content.Context
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class YandexLocationDecoder(context: Context) : LocationDecoder {

    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    override suspend fun reverseGeocode(request: GeocodingRequest): GeocodingResult {
        return suspendCancellableCoroutine { continuation ->
            val point = com.yandex.mapkit.geometry.Point(
                request.location.latitude, request.location.longitude
            )
            val options = SearchOptions().apply { resultPageSize = request.maxResults }
            val listener = object : Session.SearchListener {
                override fun onSearchResponse(response: com.yandex.mapkit.search.Response) {
                    val results = response.collection.children.map { geoObject ->
                        GeocodingAddress(
                            latitude = request.location.latitude,
                            longitude = request.location.longitude,
                            formattedAddress = geoObject.toString()
                        )
                    }
                    continuation.resume(
                        GeocodingResult(
                            formattedAddress = results.firstOrNull()?.formattedAddress,
                            results = results
                        )
                    )
                }

                override fun onSearchError(error: com.yandex.runtime.Error) {
                    continuation.resume(GeocodingResult(null, emptyList()))
                }
            }
            searchManager.submit(point, request.maxResults, options, listener)
        }
    }

    override suspend fun forwardGeocode(
        query: String, language: String, maxResults: Int
    ): List<GeocodingAddress> {
        // TODO: Yandex 4.33.1 forward text search API signature needs verification.
        // Use searchManager.submit(TextQuery, SearchOptions, SearchListener)
        return emptyList()
    }
}
