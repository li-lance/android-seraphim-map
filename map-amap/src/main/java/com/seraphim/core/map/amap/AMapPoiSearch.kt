package com.seraphim.core.map.amap

import android.content.Context
import android.util.Log
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.search.PoiSearch
import com.seraphim.core.map.commons.search.SearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.seraphim.core.map.commons.search.PoiResult as CommonsPoiResult

class AMapPoiSearch(context: Context) : PoiSearch {

    private val appContext = context.applicationContext

    companion object {
        private const val TAG = "AMapPoiSearch"
    }

    override suspend fun searchByText(query: String): SearchResult {
        return try {
            val poiQuery = com.amap.api.services.poisearch.PoiSearch.Query(query, "" /* category */)
            poiQuery.pageSize = 30

            val poiSearch = com.amap.api.services.poisearch.PoiSearch(appContext, poiQuery)
            val result = executeSearch(poiSearch)

            val items = result?.pois ?: emptyList()
            val results = items.map { it.toPoiResult() }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchByText failed: ${e.message}", e)
            SearchResult.Error("AMap text search failed: ${e.message}", e)
        }
    }

    override suspend fun searchNearby(
        query: String?,
        center: LatLng,
        radius: Double
    ): SearchResult {
        return try {
            val poiQuery =
                com.amap.api.services.poisearch.PoiSearch.Query(query ?: "", "" /* category */)
            poiQuery.pageSize = 30
            poiQuery.isDistanceSort = true

            val searchBound = com.amap.api.services.poisearch.PoiSearch.SearchBound(
                LatLonPoint(center.latitude, center.longitude),
                radius.toInt()
            )

            val poiSearch = com.amap.api.services.poisearch.PoiSearch(appContext, poiQuery)
            poiSearch.setBound(searchBound)

            val result = executeSearch(poiSearch)

            val items = result?.pois ?: emptyList()
            val results = items.map { item ->
                val poiResult = item.toPoiResult()
                // AMap distance is in meters, -1 if unavailable
                val distance = if (item.distance > 0) item.distance.toDouble() else null
                poiResult.copy(distance = distance)
            }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchNearby failed: ${e.message}", e)
            SearchResult.Error("AMap nearby search failed: ${e.message}", e)
        }
    }

    private suspend fun executeSearch(poiSearch: com.amap.api.services.poisearch.PoiSearch): PoiResult? =
        suspendCancellableCoroutine { continuation ->
            poiSearch.setOnPoiSearchListener(object :
                com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(result: PoiResult?, code: Int) {
                    if (code == AMapException.CODE_AMAP_SUCCESS) {
                        continuation.resume(result)
                    } else {
                        Log.e(TAG, "AMap search error code: $code")
                        continuation.resume(null)
                    }
                }

                override fun onPoiItemSearched(item: PoiItem?, code: Int) {
                    // Not used for bulk search
                }
            })

            poiSearch.searchPOIAsyn()

            continuation.invokeOnCancellation {
                // AMap PoiSearch doesn't support cancellation
            }
        }

    private fun PoiItem.toPoiResult(): CommonsPoiResult {
        val point = this.latLonPoint
        return CommonsPoiResult(
            id = this.poiId ?: "",
            name = this.title ?: "",
            latLng = if (point != null) {
                LatLng(point.latitude, point.longitude)
            } else {
                LatLng(0.0, 0.0)
            },
            address = this.snippet,
            distance = if (this.distance > 0) this.distance.toDouble() else null
        )
    }
}
