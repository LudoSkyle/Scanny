plugins {
    id("scanny-kmp-library")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

kotlin {
    compilerOptions {
        // Room generates the actual object that constructs the database, an expect class
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "fr.skyle.scanny.data"

        withHostTest {
            // Required by Robolectric, which runs the database tests on the JVM
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)

            // Room
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // DataStore
            implementation(libs.datastore.preferences.core)
            implementation(libs.okio)

            // Koin
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            // Koin
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
            implementation(libs.test.okio.fakefilesystem)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.robolectric)
            implementation(libs.test.androidx.core)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

// The host tests run the bundled SQLite on the JVM: its JVM natives are extracted and put on the
// library path of the test JVM, the Android ones of the app not loading there
val sqliteJvmNatives: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    sqliteJvmNatives(libs.sqlite.bundled.jvm)
}

val extractSqliteJvmNatives by tasks.registering(Sync::class) {
    from(sqliteJvmNatives.elements.map { jars -> jars.map { jar -> zipTree(jar) } }) {
        include("natives/**")
    }
    into(layout.buildDirectory.dir("sqliteJvmNatives"))
}

tasks.withType<Test>().configureEach {
    dependsOn(extractSqliteJvmNatives)

    val osName = System.getProperty("os.name").lowercase()
    val os = when {
        "mac" in osName -> "osx"
        "windows" in osName -> "windows"
        else -> "linux"
    }
    val arch = if (System.getProperty("os.arch") in listOf("aarch64", "arm64")) "arm64" else "x64"
    val nativesDirectory = layout.buildDirectory.dir("sqliteJvmNatives/natives/${os}_$arch")
    jvmArgumentProviders.add(
        CommandLineArgumentProvider {
            listOf("-Djava.library.path=${nativesDirectory.get().asFile.absolutePath}")
        }
    )
}