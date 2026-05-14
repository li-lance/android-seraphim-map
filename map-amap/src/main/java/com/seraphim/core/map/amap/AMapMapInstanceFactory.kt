package com.seraphim.core.map.amap

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.seraphim.core.map.commons.MapCredentials
import com.seraphim.core.map.commons.MapHost
import com.seraphim.core.map.commons.MapInstance
import com.seraphim.core.map.commons.MapOptions
import com.seraphim.core.map.commons.MapSdkInitializer
import com.seraphim.core.map.commons.location.LocationDecoder
import com.seraphim.core.map.commons.location.UserLocationProvider
import com.seraphim.core.map.commons.registry.MapAvailability
import com.seraphim.core.map.commons.registry.MapInstanceFactory
import com.seraphim.core.map.commons.search.PoiSearch

class AMapMapInstanceFactory : MapInstanceFactory, MapSdkInitializer {
    override val providerId = "amap"

    private var credentials: MapCredentials = MapCredentials.None

    override suspend fun checkAvailability(context: Context): MapAvailability {
        return try {
            Class.forName("com.amap.api.maps.MapView"); MapAvailability.Available
        } catch (e: ClassNotFoundException) {
            MapAvailability.Unavailable("AMap SDK not found", "Add com.amap.api:3dmap dependency")
        }
    }

    override fun createMapHost(context: Context): MapHost =
        throw UnsupportedOperationException("AMap requires ViewGroup")

    override fun createMapHost(context: Context, parent: ViewGroup): MapHost =
        AMapMapHost.create(context, parent)

    override fun createMapInstance(context: Context, options: MapOptions): MapInstance =
        AMapMapInstance()
    fun createClusterableMapInstance(ctx: Context, opts: MapOptions): AMapClusterableMap =
        AMapClusterableMap()

    override fun createUserLocationProvider(context: Context): UserLocationProvider =
        AMapUserLocationProvider(context)

    override fun createLocationDecoder(context: Context): LocationDecoder =
        AMapLocationDecoder(context)

    override fun createPoiSearch(context: Context): PoiSearch =
        AMapPoiSearch(context)

    // ── MapSdkInitializer ──

    override fun init(app: Application, credentials: MapCredentials) {
        this.credentials = credentials
        try {
            MapsInitializer.updatePrivacyShow(app, true, true)
            MapsInitializer.updatePrivacyAgree(app, true)

            try {
                AMapLocationClient.updatePrivacyShow(app, true, true)
                AMapLocationClient.updatePrivacyAgree(app, true)
            } catch (e: Exception) {
                Log.w(TAG, "AMap Location SDK not found, skip location privacy init")
            }
            Log.d(TAG, "AMap SDK initialized")
        } catch (e: Exception) {
            Log.w(TAG, "AMap SDK not found")
        }
    }

    companion object {
        private const val TAG = "AMapMapInstanceFactory"
    }
}
