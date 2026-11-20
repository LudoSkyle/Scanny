package fr.skyle.scanny.data.db

import androidx.room.Room
import fr.skyle.scanny.data.file.applicationSupportPath

internal fun ScannyDatabase.Companion.build(): ScannyDatabase =
    Room.databaseBuilder<ScannyDatabase>(
        name = applicationSupportPath(fileName = NAME),
    ).withScannyConfiguration()
        .build()