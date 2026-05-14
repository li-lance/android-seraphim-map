package com.seraphim.core.map.google

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.seraphim.core.map.commons.model.LatLng
import com.seraphim.core.map.commons.search.PoiResult
import com.seraphim.core.map.commons.search.PoiSearch
import com.seraphim.core.map.commons.search.SearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

class GooglePoiSearch(context: Context) : PoiSearch {

    private val client: PlacesClient
    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS
    )

    companion object {
        private const val TAG = "GooglePoiSearch"
    }

    init {
        if (!Places.isInitialized()) {
            // Try to initialize from manifest as last resort
            val key = readApiKeyFromManifest(context)
            if (!key.isNullOrBlank()) {
                Log.d(TAG, "Lazy-initializing Places from manifest")
                Places.initialize(context.applicationContext, key)
            } else {
                throw IllegalStateException("Places must be initialized before creating GooglePoiSearch. Call MapInitializer.init() with a valid API key.")
            }
        }
        client = Places.createClient(context)
    }

    override suspend fun searchByText(query: String): SearchResult {
        return try {
            val predictions = fetchPredictions(query, null)
            val results = predictions.mapNotNull { prediction ->
                prediction.placeId?.let { fetchPlaceDetails(it) }
            }
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
            val bounds = CircularBounds.newInstance(
                GoogleLatLng(center.latitude, center.longitude),
                radius
            )
            val predictions = fetchPredictions(query ?: "", bounds)
            val results = predictions.mapNotNull { prediction ->
                prediction.placeId?.let { placeId ->
                    val place = fetchPlaceDetails(placeId)
                    place?.let {
                        val distance = calculateDistance(
                            center,
                            it.latLng?.let { gLatLng ->
                                GoogleLatLng(
                                    gLatLng.latitude,
                                    gLatLng.longitude
                                )
                            })
                        it.copy(distance = distance)
                    }
                }
            }
            SearchResult.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchNearby failed: ${e.message}", e)
            SearchResult.Error("Nearby search failed: ${e.message}", e)
        }
    }

    private suspend fun fetchPredictions(
        query: String,
        bounds: CircularBounds?
    ): List<com.google.android.libraries.places.api.model.AutocompletePrediction> =
        suspendCancellableCoroutine { continuation ->
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries(emptyList())

            bounds?.let { requestBuilder.setLocationBias(it) }

            val request = requestBuilder.build()
            val task = client.findAutocompletePredictions(request)

            continuation.invokeOnCancellation {
                // Google Tasks don't support direct cancellation
            }

            task.addOnSuccessListener { response ->
                continuation.resume(response.autocompletePredictions)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Prediction fetch failed: ${exception.message}", exception)
                continuation.resume(emptyList())
            }
        }

    private suspend fun fetchPlaceDetails(placeId: String): PoiResult? =
        suspendCancellableCoroutine { continuation ->
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            val task = client.fetchPlace(request)

            continuation.invokeOnCancellation {
                // Google Tasks don't support direct cancellation
            }

            task.addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                val result = if (latLng != null) {
                    PoiResult(
                        id = place.id ?: placeId,
                        name = place.name ?: "",
                        latLng = LatLng(latLng.latitude, latLng.longitude),
                        address = place.address,
                        distance = null
                    )
                } else null
                continuation.resume(result)
            }.addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Place details fetch failed for $placeId: ${exception.message}",
                    exception
                )
                continuation.resume(null)
            }
        }

    private fun calculateDistance(from: LatLng, to: GoogleLatLng?): Double? {
        if (to == null) return null
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
    }

    private fun readApiKeyFromManifest(context: Context): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read API key from manifest", e)
            null
        }
    }
}
