package com.seraphim.core.map.google

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import java.io.IOException

/**
 * [LocationDecoder] implementation using Android's built-in [Geocoder].
 *
 * Note: [Geocoder] relies on Google Play Services and may not be available
 * on all devices (e.g., devices without GMS). For production use,
 * consider using the Google Maps Geocoding REST API for reliability.
 */
class GoogleLocationDecoder(
    private val context: Context
) : LocationDecoder {

    override suspend fun reverseGeocode(request: GeocodingRequest): GeocodingResult {
        return try {
            val geocoder = Geocoder(context)
            val addresses: List<Address> = geocoder.getFromLocation(
                request.location.latitude,
                request.location.longitude,
                request.maxResults
            ) ?: emptyList()

            val results = addresses.map { it.toGeocodingAddress() }
            GeocodingResult(
                formattedAddress = results.firstOrNull()?.formattedAddress,
                results = results
            )
        } catch (e: IOException) {
            Log.w(TAG, "Geocoding failed: network error", e)
            GeocodingResult(formattedAddress = null, results = emptyList())
        } catch (e: Exception) {
            Log.w(TAG, "Geocoding failed", e)
            GeocodingResult(formattedAddress = null, results = emptyList())
        }
    }

    override suspend fun forwardGeocode(
        query: String,
        language: String,
        maxResults: Int
    ): List<GeocodingAddress> {
        return try {
            val geocoder = Geocoder(context)
            val addresses: List<Address> = geocoder.getFromLocationName(query, maxResults)
                ?: emptyList()

            addresses.map { it.toGeocodingAddress() }
        } catch (e: IOException) {
            Log.w(TAG, "Forward geocoding failed: network error", e)
            emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Forward geocoding failed", e)
            emptyList()
        }
    }

    private fun Address.toGeocodingAddress(): GeocodingAddress {
        return GeocodingAddress(
            latitude = latitude,
            longitude = longitude,
            formattedAddress = getAddressLine(0) ?: "",
            country = countryName,
            countryCode = countryCode,
            region = adminArea,
            city = locality ?: subAdminArea,
            district = subLocality,
            street = thoroughfare,
            houseNumber = subThoroughfare,
            postalCode = postalCode,
            poiName = featureName
        )
    }

    companion object {
        private const val TAG = "GoogleLocationDecoder"
    }
}
