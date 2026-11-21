package fr.skyle.scanny.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A capture: the codes read in one go, one for a simple scan, all those of a multiple scan. */
@Entity(
    tableName = "scan",
    indices = [
        Index(value = ["scanned_at"]),
        Index(value = ["is_in_history"]),
    ],
)
internal data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    // Epoch milliseconds
    @ColumnInfo(name = "scanned_at")
    val scannedAt: Long,
    // False when the user keeps no history: the capture is shown once, then purged
    @ColumnInfo(name = "is_in_history", defaultValue = "1")
    val isInHistory: Boolean = true,
)