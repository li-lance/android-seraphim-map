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
    implementation(libs.androidx.core.ktx)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
