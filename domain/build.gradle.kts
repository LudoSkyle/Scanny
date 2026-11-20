plugins {
    id("scanny-kmp-library")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.domain"
    }

    sourceSets {
        commonMain.dependencies {
            // Modules
            implementation(project(":data"))

            // Koin
            implementation(libs.koin.core)
        }
    }
}