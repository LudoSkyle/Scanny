plugins {
    id("scanny-compose-library")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.designsystem"
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.composeMultiplatform.ui)
            implementation(libs.composeMultiplatform.foundation)
            implementation(libs.composeMultiplatform.material3)
            implementation(libs.composeMultiplatform.resources)
            implementation(libs.composeMultiplatform.ui.tooling.preview)
        }

        androidMain.dependencies {
            // AndroidX
            implementation(libs.androidx.core)
        }
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.scanny.designsystem.resources"
}