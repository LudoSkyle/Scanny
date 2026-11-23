package fr.skyle.scanny.data.db.relation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity

internal data class ScannedCodeWithScanTime(
    @Embedded
    val code: ScannedCodeEntity,
    // The time of the capture of the code, in epoch milliseconds
    @ColumnInfo(name = "scanned_at")
    val scannedAt: Long,
)