package com.seraphim.core.map.here

import android.content.Context
import android.util.Log
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.search.ReverseGeocodingOptions
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [LocationDecoder] implementation using HERE SDK [SearchEngine].
 *
 * Provides reverse and forward geocoding via HERE's Geocoder API.
 */
class HereLocationDecoder(
    private val context: Context
) : LocationDecoder {

    private val searchEngine: SearchEngine by lazy { SearchEngine() }

    override suspend fun reverseGeocode(request: GeocodingRequest): GeocodingResult {
        return suspendCancellableCoroutine { continuation ->
            val options = ReverseGeocodingOptions(
                LanguageCode.fromLanguageTag(request.language)
            )
            searchEngine.reverseGeocode(
                GeoCoordinates(
                    request.location.latitude,
                    request.location.longitude
                ),
                options
            ) { searchError, items ->
                if (searchError != null) {
                    Log.w(TAG, "Reverse geocoding failed: ${searchError.name}")
                    continuation.resume(GeocodingResult(null, emptyList()))
                    return@reverseGeocode
                }

                val results = items?.map { item ->
                    GeocodingAddress(
                        latitude = item.coordinates?.latitude ?: 0.0,
                        longitude = item.coordinates?.longitude ?: 0.0,
                        formattedAddress = item.address?.addressText ?: "",
                        country = item.address?.countryName,
                        countryCode = item.address?.countryCode,
                        region = item.address?.regionName,
                        city = item.address?.city,
                        district = item.address?.district,
                        street = item.address?.street,
                        houseNumber = item.address?.houseNumber,
                        postalCode = item.address?.postalCode
                    )
                } ?: emptyList()

                continuation.resume(
                    GeocodingResult(
                        formattedAddress = results.firstOrNull()?.formattedAddress,
                        results = results
                    )
                )
            }
        }
    }

    override suspend fun forwardGeocode(
        query: String,
        language: String,
        maxResults: Int
    ): List<GeocodingAddress> {
        return suspendCancellableCoroutine { continuation ->
            val options = SearchOptions(
                LanguageCode.fromLanguageTag(language),
                maxResults
            )
            searchEngine.searchByQuery(
                com.here.sdk.search.TextQuery(query),
                options
            ) { searchError, items ->
                if (searchError != null) {
                    Log.w(TAG, "Forward geocoding failed: ${searchError.name}")
                    continuation.resume(emptyList())
                    return@searchByQuery
                }

                val results = items?.map { item ->
                    GeocodingAddress(
                        latitude = item.coordinates?.latitude ?: 0.0,
                        longitude = item.coordinates?.longitude ?: 0.0,
                        formattedAddress = item.address?.addressText ?: item.title ?: "",
                        country = item.address?.countryName,
                        countryCode = item.address?.countryCode,
                        region = item.address?.regionName,
                        city = item.address?.city,
                        district = item.address?.district,
                        street = item.address?.street,
                        houseNumber = item.address?.houseNumber,
                        postalCode = item.address?.postalCode
                    )
                } ?: emptyList()

                continuation.resume(results)
            }
        }
    }

    companion object {
        private const val TAG = "HereLocationDecoder"
    }
}
