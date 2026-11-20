package fr.skyle.scanny.data.db

import android.content.Context
import androidx.room.Room

// In the databases folder of the app, which Android backs up: the history is restored on a new phone
internal fun ScannyDatabase.Companion.build(context: Context): ScannyDatabase =
    Room.databaseBuilder<ScannyDatabase>(
        context = context.applicationContext,
        name = context.getDatabasePath(NAME).absolutePath,
    ).withScannyConfiguration()
        .build()