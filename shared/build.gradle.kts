plugins {
    id("scanny-compose-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.shared"
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
            implementation(libs.composeMultiplatform.ui.tooling.preview)

            // Navigation
            implementation(libs.navigation3Multiplatform.ui)
            implementation(libs.lifecycleMultiplatform.viewmodel.navigation3)

            // Kotlinx
            implementation(libs.kotlinx.serialization.core)

            // Koin
            implementation(libs.koin.core)
        }
    }
}