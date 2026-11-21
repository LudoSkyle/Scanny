plugins {
    id("scanny-compose-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.shared"

        withHostTest {
            // Kermit logs with android.util.Log, which the JVM of the host tests does not implement:
            // a test that goes through a log would fail on that call alone
            isReturnDefaultValues = true
            // Required by Robolectric, which draws the images of the codes on the JVM
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Modules
            implementation(project(":designSystem"))
            implementation(project(":domain"))

            // Compose
            implementation(libs.composeMultiplatform.ui)
            implementation(libs.composeMultiplatform.foundation)
            implementation(libs.composeMultiplatform.resources)
            implementation(libs.composeMultiplatform.ui.tooling.preview)

            // Navigation and lifecycle
            implementation(libs.navigation3Multiplatform.ui)
            implementation(libs.lifecycleMultiplatform.runtime.compose)
            implementation(libs.lifecycleMultiplatform.viewmodel)
            implementation(libs.lifecycleMultiplatform.viewmodel.navigation3)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            // Logs
            implementation(libs.kermit)

            // QR codes and barcodes drawn from their text
            implementation(libs.qrose)
            implementation(libs.qrose.oned)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
        }

        androidMain.dependencies {
            // AndroidX
            implementation(libs.androidx.activity.compose)
            // CameraX publishes the zoom of the camera in a LiveData
            implementation(libs.androidx.lifecycle.livedata.core)

            // Camera and detection
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.compose)
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.kotlinx.coroutines.play.services)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.robolectric)
            // A second reader, which Scanny did not write, for the codes it draws
            implementation(libs.test.zxing)
        }
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.scanny.resources"
}