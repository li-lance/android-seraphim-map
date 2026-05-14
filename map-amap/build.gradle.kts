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
    implementation("com.amap.api:3dmap-location-search:11.1.200_loc11.1.200_sea9.7.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
