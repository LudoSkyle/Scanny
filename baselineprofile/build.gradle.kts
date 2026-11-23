// Generates the baseline profiles of androidApp: drives the release app on a connected device through
// its critical journeys, and writes the classes and methods they run to androidApp/src/release/generated.
// Measures its cold start too, with and without the profile
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "fr.skyle.scanny.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // The lowest version a baseline profile is collected on
        minSdk = 28
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // An emulator measures less steadily than a phone: its results are kept, as an indication
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    targetProjectPath = ":androidApp"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// On the emulator or the phone connected, rather than a device managed by Gradle
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.test.androidx.ext.junit)
    implementation(libs.test.androidx.uiautomator)
    implementation(libs.test.androidx.benchmark.macro)
}
