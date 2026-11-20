/*
 * Kotlin Multiplatform module of Scanny, built for Android and iOS. Each module declares its
 * namespace, its dependencies and, when it has some, its host tests.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        compileSdk = catalogVersion("compileSdk").toInt()
        minSdk = catalogVersion("minSdk").toInt()
    }

    iosArm64()
    iosSimulatorArm64()
}