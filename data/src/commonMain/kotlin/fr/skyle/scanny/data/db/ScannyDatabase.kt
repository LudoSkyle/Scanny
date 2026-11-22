package fr.skyle.scanny.data.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.skyle.scanny.data.db.dao.CreationDao
import fr.skyle.scanny.data.db.dao.ScanDao
import fr.skyle.scanny.data.db.entity.CreationEntity
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ConstructedBy(ScannyDatabaseConstructor::class)
@Database(
    entities = [
        ScanEntity::class,
        ScannedCodeEntity::class,
        CreationEntity::class,
    ],
    version = 6,
    autoMigrations = [
        // The flag of a capture kept out of the history
        AutoMigration(from = 1, to = 2),
        // The table of the creations
        AutoMigration(from = 2, to = 3),
        // How a creation is drawn: its colors and its shapes
        AutoMigration(from = 3, to = 4),
        // The logo of a creation
        AutoMigration(from = 4, to = 5),
        // The error correction of a creation
        AutoMigration(from = 5, to = 6),
    ],
)
internal abstract class ScannyDatabase : RoomDatabase() {

    abstract fun scanDao(): ScanDao

    abstract fun creationDao(): CreationDao

    companion object {
        const val NAME = "scanny.db"
    }
}

// Room generates the actual object of each platform
@Suppress("KotlinNoActualForExpect")
internal expect object ScannyDatabaseConstructor : RoomDatabaseConstructor<ScannyDatabase> {
    override fun initialize(): ScannyDatabase
}

// The SQLite bundled with the app rather than the one of the platform: the same on every platform
// and every version
internal fun RoomDatabase.Builder<ScannyDatabase>.withScannyConfiguration(): RoomDatabase.Builder<ScannyDatabase> =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)