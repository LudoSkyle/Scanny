import com.project.starter.easylauncher.filter.ChromeLikeFilter
import org.gradle.language.nativeplatform.internal.BuildType
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.firebase.appdistribution")
    id("com.starter.easylauncher")
}

// The private submodule holds the signing keys and the Firebase files. Without it, a fork or the CI
// still builds and tests: the debug build is signed with the default key of Android, the release is
// not signed, and Firebase is left out
val keystorePropertiesFile = rootProject.file("private/keys/keystore.properties")
val hasKeystore = keystorePropertiesFile.exists()
val keystoreProperties = Properties()
if (hasKeystore) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
val hasFirebase = rootProject.file("private/firebase/google-services.json").exists()
if (hasFirebase) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "fr.skyle.scanny"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "fr.skyle.scanny"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        // Numbered by the release workflow at each upload to the Play Store; a local build keeps 20
        versionCode = providers.gradleProperty("scanny.versionCode").orNull?.toInt() ?: 20
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasKeystore) {
            maybeCreate(BuildType.DEBUG.name).apply {
                storeFile = file(keystoreProperties["debugStoreFile"].toString())
            }
            maybeCreate(BuildType.RELEASE.name).apply {
                storeFile = file(keystoreProperties["releaseStoreFile"].toString())
                storePassword = keystoreProperties["passwordRelease"].toString()
                keyAlias = keystoreProperties["aliasRelease"].toString()
                keyPassword = keystoreProperties["passwordRelease"].toString()
            }
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName(BuildType.DEBUG.name)
            }

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "Internal"
                serviceCredentialsFile = "./google-services-account.json"
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            if (hasKeystore) {
                signingConfig = signingConfigs.getByName(BuildType.RELEASE.name)
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "Internal"
                serviceCredentialsFile = "./google-services-account.json"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/{AL2.0,LGPL2.1}"
            )
        }
    }
}

easylauncher {
    buildTypes {
        register(BuildType.DEBUG.name).configure {
            filters(
                chromeLike(
                    label = "DEBUG",
                    gravity = ChromeLikeFilter.Gravity.BOTTOM,
                    ribbonColor = "#4F9E30",
                    overlayHeight = 0.2f,
                    textSizeRatio = 0.15f
                )
            )
        }
    }
}

tasks.register("clonePrivateFiles") {
    notCompatibleWithConfigurationCache("custom")
    doLast {
        copy {
            from("../private/firebase/google-services.json")
            into("./")
        }
    }
}

project.afterEvaluate {
    getTasks().getByName("preBuild").finalizedBy("clonePrivateFiles")
}

dependencies {
    // Modules
    implementation(project(":shared"))

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // Koin
    implementation(libs.koin.android)

    // Logs
    implementation(libs.kermit)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}