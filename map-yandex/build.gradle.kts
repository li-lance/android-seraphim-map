plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.yandex"
}
dependencies {
    api(project(":core:map:commons"))
    // Yandex MapKit is hosted on Google Maven (maps.mobile artifact).
    // Requires API key: MapKitFactory.setApiKey("your_key")
    // Register at https://developer.tech.yandex.com
    implementation(libs.yandex.mapkit)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
