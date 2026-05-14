package com.seraphim.core.map.tmap

import android.content.Context
import android.util.Log
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.search.PoiResult
import com.seraphim.core.map.commons.search.PoiSearch
import com.seraphim.core.map.commons.search.SearchResult
import com.skt.tmap.TMapPoint
import com.skt.tmap.poi.TMapPOIItem
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TmapPoiSearch(context: Context) : PoiSearch {

    companion object {
        private const val TAG = "TmapPoiSearch"
    }

    override suspend fun searchByText(query: String): SearchResult {
        return try {
            val items = suspendCancellableCoroutine<List<TMapPOIItem>> { continuation ->
                com.skt.tmap.TMapData().findAllPOI(query) { result ->
                    continuation.resume(result ?: emptyList())
                }
            }
            SearchResult.Success(items.map { it.toPoiResult() })
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
            val items = suspendCancellableCoroutine<List<TMapPOIItem>> { continuation ->
                val tmapPoint = TMapPoint(center.latitude, center.longitude)
                val keyword = query ?: ""
                com.skt.tmap.TMapData().findAroundKeywordPOI(
                    tmapPoint,
                    keyword,
                    radius.toInt(),
                    50 // max results
                ) { result ->
                    continuation.resume(result ?: emptyList())
                }
            }
            val results = items.map { item ->
                val itemLatLng = LatLng(
                    item.noorLat.toDoubleOrNull() ?: 0.0,
                    item.noorLon.toDoubleOrNull() ?: 0.0
                )
                val distance = calculateDistance(center, itemLatLng)
                item.toPoiResult().copy(distance = distance)
            }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchNearby failed: ${e.message}", e)
            SearchResult.Error("Nearby search failed: ${e.message}", e)
        }
    }

    private fun TMapPOIItem.toPoiResult(): PoiResult {
        return PoiResult(
            id = this.id ?: "",
            name = this.name ?: "",
            latLng = LatLng(
                this.noorLat.toDoubleOrNull() ?: 0.0,
                this.noorLon.toDoubleOrNull() ?: 0.0
            ),
            address = this.address,
            distance = null
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
