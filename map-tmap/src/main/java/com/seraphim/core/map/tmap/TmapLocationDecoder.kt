package com.seraphim.core.map.tmap

import android.content.Context
import com.seraphim.core.map.commons.location.GeocodingAddress
import com.seraphim.core.map.commons.location.GeocodingRequest
import com.seraphim.core.map.commons.location.GeocodingResult
import com.seraphim.core.map.commons.location.LocationDecoder
import com.skt.tmap.TMapData
import com.skt.tmap.address.TMapAddressInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TmapLocationDecoder(ctx: Context) : LocationDecoder {
    private val data = TMapData()

    override suspend fun reverseGeocode(req: GeocodingRequest): GeocodingResult {
        return suspendCancellableCoroutine { cont ->
            data.reverseGeocoding(
                req.location.latitude, req.location.longitude, "A10",
                object : TMapData.OnReverseGeocodingListener {
                    override fun onReverseGeocoding(info: TMapAddressInfo?) {
                        val addr = if (info != null) {
                            listOfNotNull(
                                info.strCity_do, info.strGu_gun, info.strLegalDong,
                                info.strRi, info.strBunji
                            ).joinToString(" ")
                        } else ""
                        val r =
                            GeocodingAddress(req.location.latitude, req.location.longitude, addr)
                        cont.resume(GeocodingResult(r.formattedAddress, listOf(r)))
                    }
                })
        }
    }

    override suspend fun forwardGeocode(
        query: String,
        language: String,
        maxResults: Int
    ): List<GeocodingAddress> {
        return suspendCancellableCoroutine { cont ->
            data.findAllPOI(query, object : TMapData.OnFindAllPOIListener {
                override fun onFindAllPOI(pois: ArrayList<com.skt.tmap.poi.TMapPOIItem>?) {
                    val results = pois?.map {
                        val pt = it.getPOIPoint()
                        GeocodingAddress(pt.latitude, pt.longitude, it.name ?: "")
                    } ?: emptyList()
                    cont.resume(results)
                }
            })
        }
    }
}
