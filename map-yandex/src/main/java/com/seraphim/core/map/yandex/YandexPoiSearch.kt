package com.seraphim.core.map.yandex

import android.content.Context
import android.util.Log
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.search.PoiResult
import com.seraphim.core.map.commons.search.PoiSearch
import com.seraphim.core.map.commons.search.SearchResult
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class YandexPoiSearch(context: Context) : PoiSearch {

    private val searchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.COMBINED)

    companion object {
        private const val TAG = "YandexPoiSearch"
    }

    override suspend fun searchByText(query: String): SearchResult {
        return executeSearch(query, null)
    }

    override suspend fun searchNearby(
        query: String?,
        center: LatLng,
        radius: Double
    ): SearchResult {
        return executeSearch(query ?: "", Point(center.latitude, center.longitude))
    }

    private suspend fun executeSearch(
        query: String,
        center: Point?
    ): SearchResult = suspendCancellableCoroutine { continuation ->
        val options = SearchOptions().apply {
            geometry = true
        }

        val listener = object : Session.SearchListener {
            override fun onSearchResponse(response: com.yandex.mapkit.search.Response) {
                val results = response.collection.children.mapNotNull { item ->
                    val obj = item.obj ?: return@mapNotNull null
                    val point = obj.geometry.firstOrNull()?.point ?: return@mapNotNull null
                    PoiResult(
                        id = obj.name ?: "",
                        name = obj.name ?: "",
                        latLng = LatLng(point.latitude, point.longitude),
                        address = obj.descriptionText,
                        distance = null
                    )
                }
                continuation.resume(SearchResult.Success(results))
            }

            override fun onSearchError(error: com.yandex.runtime.Error) {
                val msg = "Yandex search error: $error"
                Log.e(TAG, msg)
                continuation.resume(SearchResult.Error(msg))
            }
        }

        val session = if (center != null) {
            searchManager.submit(
                query,
                com.yandex.mapkit.geometry.Geometry.fromPoint(center),
                options,
                listener
            )
        } else {
            // For text-only search without geometry, use a point search with large radius
            searchManager.submit(
                query,
                com.yandex.mapkit.geometry.Geometry.fromPoint(Point(0.0, 0.0)),
                options,
                listener
            )
        }

        continuation.invokeOnCancellation {
            session.cancel()
        }
    }
}
