plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.here"
}
dependencies {
    api(project(":core:map:commons"))
    implementation(libs.androidx.annotation.jvm)
    // HERE SDK 4.25.5 — local AAR (61MB, exceeds GitHub Packages stability limit)
    // Copy heresdk-explore-android-4.25.5.0.274356.aar to libs/
    // Or download from GitHub Release: https://github.com/li-lance/android-seraphim-map/releases
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
