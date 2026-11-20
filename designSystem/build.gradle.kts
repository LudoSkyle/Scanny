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
            implementation(libs.composeMultiplatform.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.scanny.designsystem.resources"
}