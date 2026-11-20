package fr.skyle.scanny.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_code",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scan_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["scan_id"]),
    ],
)
internal data class ScannedCodeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "scan_id")
    val scanId: Long,
    // The order in which the code was read during its capture, from 0
    @ColumnInfo(name = "position")
    val position: Int,
    // The text of the code, as read by the detection engine
    @ColumnInfo(name = "raw_value")
    val rawValue: String,
    // The name of a BarcodeFormat of domain
    @ColumnInfo(name = "format")
    val format: String,
)