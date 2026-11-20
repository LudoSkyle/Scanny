package fr.skyle.scanny.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.skyle.scanny.data.db.dao.ScanDao
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ConstructedBy(ScannyDatabaseConstructor::class)
@Database(
    entities = [
        ScanEntity::class,
        ScannedCodeEntity::class,
    ],
    version = 1,
)
internal abstract class ScannyDatabase : RoomDatabase() {

    abstract fun scanDao(): ScanDao

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