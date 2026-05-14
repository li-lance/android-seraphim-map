package com.seraphim.core.map.commons.search

import com.seraphim.core.map.commons.model.LatLng

/**
 * Unified POI search interface across all map providers.
 *
 * Each provider implements this using its native search SDK:
 * - Google: Places API (New) — searchNearby, searchText
 * - AMap: PoiSearch — query + nearby
 * - HERE: SearchEngine — discover, browse
 * - TMap: TMapData — findAllPOI, findAroundKeywordPOI
 * - Yandex: Search — searchByText, searchNearby
 */
interface PoiSearch {

    /**
     * Search for places by text query (global or biased by optional center).
     *
     * @param query The search text (e.g., "Starbucks", "gas station").
     * @return [SearchResult.Success] with a list of [PoiResult], or [SearchResult.Error].
     */
    suspend fun searchByText(query: String): SearchResult

    /**
     * Search for places near a specific location.
     *
     * @param query Optional keyword filter (e.g., "coffee"). Empty or blank for all POIs.
     * @param center The center point of the search area.
     * @param radius Search radius in meters.
     * @return [SearchResult.Success] with a list of [PoiResult], or [SearchResult.Error].
     */
    suspend fun searchNearby(
        query: String?,
        center: LatLng,
        radius: Double
    ): SearchResult
}

/**
 * A POI search result item.
 *
 * @property id Provider-specific place identifier.
 * @property name Display name of the place.
 * @property latLng Geographic coordinates.
 * @property address Human-readable address (may be null if unavailable).
 * @property distance Distance from the search center in meters (null for text search without center).
 */
data class PoiResult(
    val id: String,
    val name: String,
    val latLng: LatLng,
    val address: String?,
    val distance: Double?
)

/**
 * Sealed result type for POI search operations.
 */
sealed class SearchResult {
    /**
     * Search succeeded. [results] may be empty if no places were found.
     */
    data class Success(val results: List<PoiResult>) : SearchResult()

    /**
     * Search failed. [message] is a human-readable error description.
     */
    data class Error(val message: String, val cause: Throwable? = null) : SearchResult()
}
