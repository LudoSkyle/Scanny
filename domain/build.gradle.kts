plugins {
    id("scanny-kmp-library")
}

kotlin {
    android {
        namespace = "fr.skyle.scanny.domain"

        withHostTest {
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Modules
            implementation(project(":data"))

            // Koin
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
        }
    }
}