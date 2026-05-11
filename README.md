# android-seraphim-map

[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-com.seraphim.map-blue)](https://github.com/li-lance/android-seraphim-map/packages)

Unified map abstraction layer for Android. Write once, run on **Google Maps**, **AMap**, **HERE SDK**, **Yandex MapKit**, and **Tmap**.

---

## Modules

| Module | Description | Clustering | Status |
|--------|-------------|-----------|--------|
| `commons` | Core interfaces, data models, MapInitializer, MapFragment, MapProviders | — | ✅ |
| `map-google` | Google Maps (Play Services) | ✅ ClusterManager | ✅ |
| `map-amap` | 高德地图 AMap 3D SDK | ⚠️ TODO | ✅ |
| `map-here` | HERE SDK Explore Edition | ❌ | ⚠️ Requires HERE auth |
| `map-yandex` | Yandex MapKit 4.33.1 | ✅ ClusterizedPlacemarkCollection | ✅ |
| `map-tmap` | Tmap SDK 3.5 (VSM 2.0.0) | ✅ TMapMarkerCluster | ✅ |

## Architecture

```
MapInitializer (Application init)
└── init(app, MapProviders.AMAP) → activeProvider set

MapProviderRegistry.instance (global singleton)
└── register(Factory) / get(providerId)

MapFragment (Fragment)
└── initialize(registry, providerId) → auto lifecycle

MapInstance (interface)
├── camera: MapCamera          ← moveTo / animateTo / zoom
├── uiSettings: MapUiSettings  ← gestures / traffic / controls
├── addMarker() / addPolyline() / addPolygon() / addCircle()
└── onMapClick / onMarkerClick / onCameraChange (property-style)
```

## Quick Start

This repo is part of `android-seraphim-framework` managed by [git-repo](https://gerrit.googlesource.com/git-repo/):

```bash
repo init -u git@github.com:li-lance/android-seraphim-framework.git -m manifests/default.xml
repo sync
```

Or as Maven dependencies via GitHub Packages:

```kotlin
// settings.gradle.kts
maven("https://maven.pkg.github.com/li-lance/android-seraphim-map") {
    credentials {
        username = providers.gradleProperty("GITHUB_PACKAGES_USER").orNull ?: ""
        password = providers.gradleProperty("GITHUB_PACKAGES_TOKEN").orNull ?: ""
    }
}

// build.gradle.kts
dependencies {
    implementation("com.seraphim.map:map-commons:1.0.0")
    implementation("com.seraphim.map:map-amap:1.0.0")
}
```

## Usage

### Step 1: Application initialization

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Init SDK (privacy compliance, API keys) + set active provider
        MapInitializer.init(this, MapProviders.AMAP)

        // Register provider factories into global singleton
        MapProviderRegistry.instance.register(AMapMapInstanceFactory())
    }
}
```

### Step 2: Use MapFragment

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registry = MapProviderRegistry.instance
        val mapFragment = MapFragment().apply {
            initialize(registry, MapInitializer.activeProvider)
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.map_container, mapFragment)
            .commit()

        scope.launch {
            val map = mapFragment.getMap()
            map.addMarker(MarkerOptions(LatLng(39.90, 116.40), "Beijing"))
        }
    }
}
```

### Provider switching

```kotlin
// Switch at runtime
mapFragment.switchProvider(MapProviders.TMAP)
```

## Provider Setup

### AMap (高德)

AMap requires privacy compliance + SHA1 API key registration.

**1. SHA1 key registration:**
```bash
# Get SHA1 from logcat (printed at app startup):
adb logcat -s MapApplication
# W/MapApplication: SHA1: 915027FC41A303544C23491A8201108E464E3925
```
Register the SHA1 at https://console.amap.com/dev/key/app

**2. AndroidManifest.xml:**
```xml
<meta-data android:name="com.amap.api.v2.apikey" android:value="YOUR_AMAP_KEY"/>
```

**3. Privacy compliance** — handled automatically by `MapInitializer.init()`. No extra code needed.

### Google Maps

```xml
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
// Handled by MapInitializer with apiKey parameter:
MapInitializer.init(this, MapProviders.YANDEX, apiKey = "your_key")
```

### Tmap
Dependencies hosted on GitHub Packages:
```kotlin
implementation("com.skt.tmap:tmap-sdk:3.5")
implementation("com.skt.tmap:vsm-tmap-sdk:2.0.0")
```

Upload custom AARs:
```bash
export GITHUB_PACKAGES_USER=li-lance
export GITHUB_PACKAGES_TOKEN=ghp_xxxx
bash scripts/upload-aar-to-github.sh path/to/lib.aar com.example artifact-id 1.0.0
```

## Publishing

```bash
# ~/.gradle/gradle.properties:
# GITHUB_PACKAGES_USER=your_username
# GITHUB_PACKAGES_TOKEN=your_token (scope: write:packages)

./gradlew publish
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| `MapInitializer.init(app, providerId)` | Single-source-of-truth for active provider + SDK init |
| `MapProviderRegistry.instance` | Global singleton, no DI framework required |
| `MapInitializer.activeProvider` | Read current provider from anywhere |
| MapFragment for lifecycle | Auto-manages host lifecycle, supports runtime switching |
| `MapProviders` constants | Type-safe provider IDs |
| Android-only (not KMP) | Map rendering is inherently platform-specific |
| Property-style listeners | Cleaner than 10+ setOnXxxListener methods |
| GitHub Packages for SDKs | Centralized dependency management |

## License

Apache 2.0
