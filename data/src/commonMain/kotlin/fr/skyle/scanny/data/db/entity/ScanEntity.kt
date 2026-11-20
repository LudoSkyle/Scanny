package fr.skyle.scanny.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    // The text of the code, as read by the detection engine
    @ColumnInfo(name = "raw_value")
    val rawValue: String,
    // The name of a BarcodeFormat of domain
    @ColumnInfo(name = "format")
    val format: String,
    // Epoch milliseconds
    @ColumnInfo(name = "scanned_at")
    val scannedAt: Long,
)