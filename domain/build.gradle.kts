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
            // The days and times of an event are in the public contents
            api(libs.kotlinx.datetime)

            // Koin
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
        }
    }
}