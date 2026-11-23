plugins {
    `kotlin-dsl`
}

// The plugins of every module are on this classpath, and not in the root build script: the
// convention plugins below configure them, and they must be loaded by the same class loader
dependencies {
    // Android
    implementation(libs.plugin.android)

    // Kotlin
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.kotlinSerialization)
    implementation(libs.plugin.composeCompiler)
    implementation(libs.plugin.composeMultiplatform)

    // KSP
    implementation(libs.plugin.ksp)

    // Room
    implementation(libs.plugin.room)

    // Firebase
    implementation(libs.plugin.googleServices)
    implementation(libs.plugin.firebaseCrashlytics)
    implementation(libs.plugin.firebaseAppdistribution)

    // App
    implementation(libs.plugin.easylauncher)
    implementation(libs.plugin.baselineprofile)
}