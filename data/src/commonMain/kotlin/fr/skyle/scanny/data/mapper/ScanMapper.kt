package fr.skyle.scanny.data.mapper

import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.model.StoredScan
import kotlin.time.Instant

internal fun ScanEntity.toStoredScan(): StoredScan =
    StoredScan(
        id = id,
        rawValue = rawValue,
        format = format,
        scannedAt = Instant.fromEpochMilliseconds(scannedAt),
    )