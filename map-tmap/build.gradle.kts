plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.tmap"
}
dependencies {
    api(project(":core:map:commons"))
    // Tmap SDK: download AAR from https://tmapapi.tmapmobility.com
    // Place the AAR in libs/ and add:
    //   implementation(fileTree("libs") { include("*.aar") })
    compileOnly(libs.tmap.sdk)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
