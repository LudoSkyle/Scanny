/*
 * Kotlin Multiplatform module of Scanny with a Compose Multiplatform UI.
 */
plugins {
    id("scanny-kmp-library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    android {
        // Compose Resources ships the strings, fonts and drawables as Android resources
        androidResources {
            enable = true
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))
}

dependencies {
    // Renders the previews of the common code in Android Studio
    "androidRuntimeClasspath"(catalogLibrary("composeMultiplatform-ui-tooling"))
}