package fr.skyle.scanny.data.db

import fr.skyle.scanny.data.db.dao.CreationDao
import fr.skyle.scanny.data.db.dao.ScanDao
import kotlin.test.AfterTest

/**
 * Runs the real Room database in memory, on the JVM through Robolectric and on the iOS simulator.
 *
 * The database is created with the test instance, and not in a `@BeforeTest`: Kotlin/Native does
 * not run the `@BeforeTest` of a parent class before those of its subclasses.
 */
internal abstract class DatabaseTest : PlatformTest() {

    protected val database: ScannyDatabase = inMemoryScannyDatabase()

    protected val scanDao: ScanDao
        get() = database.scanDao()

    protected val creationDao: CreationDao
        get() = database.creationDao()

    @AfterTest
    fun closeDatabase() {
        database.close()
    }
}

// Base class of the tests that need the platform, the Android framework for instance
internal expect abstract class PlatformTest()

internal expect fun inMemoryScannyDatabase(): ScannyDatabase