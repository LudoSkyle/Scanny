package fr.skyle.scanny.data.db

import androidx.room.Room

internal actual abstract class PlatformTest actual constructor()

internal actual fun inMemoryScannyDatabase(): ScannyDatabase =
    Room.inMemoryDatabaseBuilder<ScannyDatabase>()
        .withScannyConfiguration()
        .build()