package com.seraphim.core.map.commons.search

// TODO: Implement place search interface.
//  This will provide a unified API for searching POIs and places,
//  including nearby search and text-based search.
//
//  Target capabilities across providers:
//  - Google Maps: Places API (findPlace, nearbySearch, textSearch)
//  - HERE: Places (Discover / Browse) API
//  - Yandex: Search API
//  - Tmap: POI Search API
//
//  Proposed interface:
//  interface PlaceSearch {
//      suspend fun searchNearby(location: LatLng, radius: Double, type: String?): List<PlaceResult>
//      suspend fun searchByText(query: String, ...): List<PlaceResult>
//  }

/**
 * Placeholder for future PlaceSearch implementation.
 */
@Suppress("unused")
interface PlaceSearch {
    // TODO: Define search API
}
