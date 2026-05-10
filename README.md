# android-seraphim-map

[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-com.seraphim.map-blue)](https://github.com/li-lance/android-seraphim-map/packages)

Unified map abstraction layer for Android. Write once, run on **Google Maps**, **HERE SDK**, **Yandex MapKit**, and **Tmap**.

---

## Modules

| Module | Description | Clustering | Status |
|--------|-------------|-----------|--------|
| `commons` | Core interfaces, data models, registry | — | ✅ |
| `map-google` | Google Maps (Play Services) | ✅ ClusterManager | ✅ |
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

```kotlin
val registry = MapProviderRegistry()
registry.register(GoogleMapInstanceFactory())

val host = registry.get("google").createMapHost(context, parent)
val map = registry.get("google").createMapInstance(context, MapOptions(
    initialCamera = InitialCamera.Position(LatLng(37.56, 126.97), zoom = 14f)
))

scope.launch {
    map.init(host, options)
    map.addMarker(MarkerOptions(position = LatLng(37.5665, 126.9780), title = "Seoul"))
    map.onMapClick = { location -> map.camera.animateTo(location, zoom = 16f) }
}
```

## Provider Setup

### Google Maps
```xml
<!-- AndroidManifest.xml -->
<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_KEY"/>
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
Use `scripts/upload-aar-to-github.sh` to upload AARs to GitHub Packages:
```bash
export GITHUB_PACKAGES_USER=li-lance
export GITHUB_PACKAGES_TOKEN=ghp_xxxx
bash scripts/upload-aar-to-github.sh \
  path/to/lib.aar com.example artifact-id 1.0.0
```

## Publishing

Modules use `seraphim.vmaven` convention plugin for GitHub Packages:

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
| Provider-specific factories | Each provider manages its own SDK lifecycle |
| Android-only (not KMP) | Map rendering is inherently platform-specific |
| Property-style listeners | Cleaner than 10+ setOnXxxListener methods |
| GitHub Packages for third-party SDKs | Centralized dependency management, no manual AAR placement |

## License

Apache 2.0
