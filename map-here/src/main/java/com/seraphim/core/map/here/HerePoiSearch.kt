package com.seraphim.core.map.here

import android.content.Context
import android.util.Log
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.Place
import com.here.sdk.search.SearchCallback
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.search.PoiResult
import com.seraphim.core.map.commons.search.PoiSearch
import com.seraphim.core.map.commons.search.SearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class HerePoiSearch(context: Context) : PoiSearch {

    private val searchEngine: SearchEngine

    init {
        searchEngine = try {
            SearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("HERE SearchEngine init failed: ${e.error.name}")
        }
    }

    companion object {
        private const val TAG = "HerePoiSearch"
    }

    override suspend fun searchByText(query: String): SearchResult {
        return try {
            val textQuery = TextQuery(query, TextQuery.Area(GeoCoordinates(0.0, 0.0)))
            val options = SearchOptions().apply {
                maxItems = 30
            }
            val places = executeSearch(textQuery, options)
            val results = places.map { it.toPoiResult() }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchByText failed: ${e.message}", e)
            SearchResult.Error("Text search failed: ${e.message}", e)
        }
    }

    override suspend fun searchNearby(
        query: String?,
        center: LatLng,
        radius: Double
    ): SearchResult {
        return try {
            val geoCenter = GeoCoordinates(center.latitude, center.longitude)
            val geoCircle = GeoCircle(geoCenter, radius)
            val options = SearchOptions().apply {
                maxItems = 30
            }
            val places = executeSearch(geoCircle, query ?: "", options)
            val results = places.map { place ->
                val poiResult = place.toPoiResult()
                // Use distance from HERE if available, otherwise calculate
                val distance = place.distanceInMeters?.toDouble()
                    ?: calculateDistance(center, poiResult.latLng)
                poiResult.copy(distance = distance)
            }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchNearby failed: ${e.message}", e)
            SearchResult.Error("Nearby search failed: ${e.message}", e)
        }
    }

    private suspend fun executeSearch(
        query: TextQuery,
        options: SearchOptions
    ): List<Place> = suspendCancellableCoroutine { continuation ->
        val taskHandle = searchEngine.searchByText(
            query, options,
            SearchCallback { searchError, list ->
                if (searchError != null) {
                    Log.e(TAG, "Search error: $searchError")
                    continuation.resume(emptyList())
                } else {
                    continuation.resume(list ?: emptyList())
                }
            }
        )

        continuation.invokeOnCancellation {
            taskHandle.cancel()
        }
    }

    private suspend fun executeSearch(
        geoCircle: GeoCircle,
        query: String,
        options: SearchOptions
    ): List<Place> = suspendCancellableCoroutine { continuation ->
        val taskHandle = searchEngine.search(
            geoCircle, options,
            SearchCallback { searchError, list ->
                if (searchError != null) {
                    Log.e(TAG, "Search error: $searchError")
                    continuation.resume(emptyList())
                } else {
                    continuation.resume(list ?: emptyList())
                }
            }
        )

        continuation.invokeOnCancellation {
            taskHandle.cancel()
        }
    }

    private fun Place.toPoiResult(): PoiResult {
        val coords = this.geoCoordinates
        return PoiResult(
            id = this.id ?: "",
            name = this.title ?: "",
            latLng = if (coords != null) {
                LatLng(coords.latitude, coords.longitude)
            } else {
                LatLng(0.0, 0.0)
            },
            address = this.address?.addressText,
            distance = this.distanceInMeters?.toDouble()
        )
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double? {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
    }
}
