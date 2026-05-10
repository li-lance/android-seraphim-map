package com.seraphim.core.map.commons

/**
 * Authentication credentials for map providers.
 * Most hosts will initialize the provider SDK separately;
 * this is a fallback for cases where initialization needs to happen inside the module.
 */
sealed class MapCredentials {
    /** No credentials — host is responsible for SDK initialization. */
    object None : MapCredentials()

    /** Simple API key. */
    data class ApiKey(val key: String) : MapCredentials()

    /** HERE SDK-style access key ID + secret pair. */
    data class HereCredentials(val accessKeyId: String, val accessKeySecret: String) :
        MapCredentials()

    /** OAuth bearer token. */
    data class OAuth(val token: String) : MapCredentials()
}
