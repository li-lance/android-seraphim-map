plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.tmap"
}
dependencies {
    api(project(":core:map:commons"))
    implementation("com.skt.tmap:tmap-sdk:3.5")
    implementation("com.skt.tmap:vsm-tmap-sdk:2.0.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
