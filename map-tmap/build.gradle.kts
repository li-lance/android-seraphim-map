plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.tmap"
}
dependencies {
    api(project(":core:map:commons"))
    // Tmap SDK AARs (v3.5 + VSM v2.0.0)
    implementation(fileTree("libs") { include("*.aar") })
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
