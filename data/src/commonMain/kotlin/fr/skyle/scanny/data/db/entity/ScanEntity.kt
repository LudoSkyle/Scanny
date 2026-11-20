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
    ],
)
internal data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    // Epoch milliseconds
    @ColumnInfo(name = "scanned_at")
    val scannedAt: Long,
)