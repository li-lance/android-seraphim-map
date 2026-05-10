plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.google"
}
dependencies {
    api(project(":core:map:commons"))
    api(libs.google.maps)
    api(libs.google.maps.utils)
    implementation(libs.google.location)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
