plugins {
    alias(libs.plugins.seraphim.android.library)
    alias(libs.plugins.seraphim.vmaven)
}
android {
    namespace = "com.seraphim.core.map.here"
}
dependencies {
    api(project(":core:map:commons"))
    // HERE SDK requires authentication. See docs for setup.
    // 1. Register at https://platform.here.com
    // 2. Add credentials to ~/.gradle/gradle.properties:
    //    HERE_ACCESS_KEY_ID=your_key_id
    //    HERE_ACCESS_KEY_SECRET=your_key_secret
    // 3. Uncomment the implementation line below and switch to implementation
    compileOnly(libs.here.sdk)
    // implementation(libs.here.sdk)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
