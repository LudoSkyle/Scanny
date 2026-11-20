package fr.skyle.scanny.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Inherited by the subclasses: they all run with the Android framework of Robolectric
@RunWith(RobolectricTestRunner::class)
internal actual abstract class PlatformTest actual constructor()

internal actual fun inMemoryScannyDatabase(): ScannyDatabase =
    Room.inMemoryDatabaseBuilder<ScannyDatabase>(
        context = ApplicationProvider.getApplicationContext(),
    ).setDriver(BundledSQLiteDriver())
        .build()