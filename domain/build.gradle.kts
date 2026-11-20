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

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
        }
    }
}