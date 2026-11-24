package fr.skyle.scanny.data.mapper

import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import fr.skyle.scanny.data.db.relation.ScanWithCodes
import fr.skyle.scanny.data.db.relation.ScannedCodeWithScanTime
import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import kotlin.time.Instant

internal fun ScanWithCodes.toStoredScan(): StoredScan {
    val scannedAt = Instant.fromEpochMilliseconds(scan.scannedAt)
    return StoredScan(
        id = scan.id,
        scannedAt = scannedAt,
        codes = codes
            .sortedBy { code -> code.position }
            .map { code ->
                code.toStoredCode(
                    scannedAt = scannedAt,
                    isScanFavorite = scan.isFavorite,
                    scanCodeCount = codes.size,
                )
            },
        isFavorite = scan.isFavorite,
    )
}

internal fun ScannedCodeWithScanTime.toStoredCode(): StoredCode =
    code.toStoredCode(
        scannedAt = Instant.fromEpochMilliseconds(scannedAt),
        isScanFavorite = isScanFavorite,
        scanCodeCount = scanCodeCount,
    )

private fun ScannedCodeEntity.toStoredCode(
    scannedAt: Instant,
    isScanFavorite: Boolean,
    scanCodeCount: Int,
): StoredCode =
    StoredCode(
        id = id,
        scanId = scanId,
        rawValue = rawValue,
        format = format,
        scannedAt = scannedAt,
        isScanFavorite = isScanFavorite,
        scanCodeCount = scanCodeCount,
    )