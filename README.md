# android-seraphim-map

[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-seraphim.map-blue)](https://github.com/li-lance/android-seraphim-map/packages)

Unified map abstraction layer for Android. Write once, run on **Google Maps**, **HERE SDK**, **Yandex MapKit**, and **Tmap**.

---

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| `commons` | Core interfaces, data models, registry | ✅ |
| `map-google` | Google Maps (Play Services) | ✅ |
| `map-here` | HERE SDK | ⚠️ Requires HERE auth |
| `map-yandex` | Yandex MapKit 4.33.1 | ✅ |
| `map-tmap` | Tmap SDK | ⚠️ Requires AAR |

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

This repo is part of the `android-seraphim-framework` managed by [git-repo](https://gerrit.googlesource.com/git-repo/). Clone via:

```bash
repo init -u git@github.com:li-lance/android-seraphim-framework.git -m manifests/default.xml
repo sync
```

Or use directly as a standalone dependency via GitHub Packages:

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
// 1. Register providers
val registry = MapProviderRegistry()
registry.register(GoogleMapInstanceFactory())

// 2. Create map host and instance
val host = registry.get("google").createMapHost(context, parent)
val map = registry.get("google").createMapInstance(context, MapOptions(
    initialCamera = InitialCamera.Position(LatLng(37.56, 126.97), zoom = 14f)
))

// 3. Initialize and use
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
Add credentials to `~/.gradle/gradle.properties`:
```properties
HERE_ACCESS_KEY_ID=your_key
HERE_ACCESS_KEY_SECRET=your_secret
```

### Yandex MapKit
```kotlin
MapKitFactory.setApiKey("your_api_key")
MapKitFactory.initialize(context)
```

### Tmap
Download AAR from [Tmap Mobility](https://tmapapi.tmapmobility.com), place in `app/libs/`.

## Publishing

Modules use `seraphim.vmaven` convention plugin for GitHub Packages:

```bash
# Set credentials in ~/.gradle/gradle.properties:
# GITHUB_PACKAGES_USER=your_username
# GITHUB_PACKAGES_TOKEN=your_token (scope: write:packages)

./gradlew publish
```

## License

Apache 2.0
