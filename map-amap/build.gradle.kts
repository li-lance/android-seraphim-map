plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.amap"
}
dependencies {
    api(project(":core:map:commons"))
    api(libs.amap.map)
    // AMap location & search — add when version confirmed:
    // implementation(libs.amap.location)
    // implementation(libs.amap.search)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
