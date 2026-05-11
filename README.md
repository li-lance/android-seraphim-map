# android-seraphim-map

[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-com.seraphim.map-blue)](https://github.com/li-lance/android-seraphim-map/packages)

Unified map abstraction layer for Android. Write once, run on **Google Maps**, **AMap**, **HERE SDK**, **Yandex MapKit**, and **Tmap**.

---

## Modules

| Module | Description | Clustering | Status |
|--------|-------------|-----------|--------|
| `commons` | Core interfaces, data models, registry, MapFragment, MapProviders | — | ✅ |
| `map-google` | Google Maps (Play Services) | ✅ ClusterManager | ✅ |
| `map-amap` | 高德地图 AMap 3D SDK | ⚠️ TODO | ✅ |
| `map-here` | HERE SDK Explore Edition | ❌ | ⚠️ Requires HERE auth |
| `map-yandex` | Yandex MapKit 4.33.1 | ✅ ClusterizedPlacemarkCollection | ✅ |
| `map-tmap` | Tmap SDK 3.5 (VSM 2.0.0) | ✅ TMapMarkerCluster | ✅ |

## Architecture

```
MapInstance (interface)
├── camera: MapCamera          ← moveTo / animateTo / zoom / bounds
├── uiSettings: MapUiSettings  ← gestures / traffic / controls
├── addMarker() / addPolyline() / addPolygon() / addCircle()
├── onMapClick / onMarkerClick / onCameraChange (property-style)
└── ClusterableMap (separate interface for clustering)

MapHost (interface)
└── awaitNativeMap() + lifecycle callbacks

MapFragment (Fragment)
└── initialize(registry, MapProviders.XXX)  ← auto lifecycle + provider switching

MapProviderRegistry
└── register(Factory) / get(providerId) / dispose()
```

## Quick Start

This repo is part of `android-seraphim-framework` managed by [git-repo](https://gerrit.googlesource.com/git-repo/). Clone via:

```bash
repo init -u git@github.com:li-lance/android-seraphim-framework.git -m manifests/default.xml
repo sync
```

Or add as a Maven dependency via GitHub Packages:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.github.com/li-lance/android-seraphim-map") {
            credentials {
                username = providers.gradleProperty("GITHUB_PACKAGES_USER").orNull ?: ""
                password = providers.gradleProperty("GITHUB_PACKAGES_TOKEN").orNull ?: ""
            }
        }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.seraphim.map:map-commons:1.0.0")
    implementation("com.seraphim.map:map-google:1.0.0")
}
```

## Usage

### MapFragment (recommended)

```kotlin
val registry = MapProviderRegistry()
registry.register(GoogleMapInstanceFactory())

val mapFragment = MapFragment().apply {
    initialize(registry, MapProviders.GOOGLE)
}
supportFragmentManager.beginTransaction()
    .replace(android.R.id.content, mapFragment)
    .commit()

scope.launch {
    val map = mapFragment.getMap()
    map.addMarker(MarkerOptions(LatLng(37.56, 126.97), "Seoul"))
}

// Switch provider at runtime
mapFragment.switchProvider(MapProviders.AMAP)
```

### Manual (MapHost)

```kotlin
val registry = MapProviderRegistry()
registry.register(AMapMapInstanceFactory())

val host = registry.get(MapProviders.AMAP).createMapHost(context, parent)
val map = registry.get(MapProviders.AMAP).createMapInstance(context, MapOptions(
    initialCamera = InitialCamera.Position(LatLng(37.56, 126.97), zoom = 14f)
))

scope.launch {
    map.init(host, options)
    map.addMarker(MarkerOptions(LatLng(37.5665, 126.9780), title = "Seoul"))
}
```

## Provider Setup

### Google Maps
```xml
<!-- AndroidManifest.xml -->
<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_KEY"/>
```

### AMap (高德)
```kotlin
// No special setup needed — AMap 3D SDK handles privacy compliance internally.
// Dependency:
implementation("com.amap.api:3dmap:10.0.600")
```

### HERE SDK
```properties
# ~/.gradle/gradle.properties
HERE_ACCESS_KEY_ID=your_key
HERE_ACCESS_KEY_SECRET=your_secret
```

### Yandex MapKit
```kotlin
MapKitFactory.setApiKey("your_api_key")
MapKitFactory.initialize(context)
```

### Tmap
No SDK download needed — dependencies are hosted on GitHub Packages:
```kotlin
implementation("com.skt.tmap:tmap-sdk:3.5")
implementation("com.skt.tmap:vsm-tmap-sdk:2.0.0")
```

### Uploading third-party AARs
Use `scripts/upload-aar-to-github.sh`:
```bash
export GITHUB_PACKAGES_USER=li-lance
export GITHUB_PACKAGES_TOKEN=ghp_xxxx
bash scripts/upload-aar-to-github.sh path/to/lib.aar com.example artifact-id 1.0.0
```

## Publishing

```bash
# Set credentials in ~/.gradle/gradle.properties:
# GITHUB_PACKAGES_USER=your_username
# GITHUB_PACKAGES_TOKEN=your_token (scope: write:packages)

./gradlew publish
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| No DI framework required | Registry pattern works with any DI or none |
| Clustering as separate interface | Flexibility for providers without native support |
| MapFragment for lifecycle | Auto-manages host lifecycle, supports runtime provider switching |
| MapProviders constants | Type-safe provider IDs, no magic strings |
| Android-only (not KMP) | Map rendering is inherently platform-specific |
| Property-style listeners | Cleaner than 10+ setOnXxxListener methods |
| GitHub Packages for third-party SDKs | Centralized dependency management, no manual AAR placement |

## License

Apache 2.0
