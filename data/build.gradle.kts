plugins {
    id("scanny-kmp-library")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.data"
    }

    sourceSets {
        commonMain.dependencies {
            // Koin
            implementation(libs.koin.core)
        }
    }
}