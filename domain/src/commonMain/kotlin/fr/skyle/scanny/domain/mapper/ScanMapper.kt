package fr.skyle.scanny.domain.mapper

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.payload.BarcodeContentParser

internal fun StoredScan.toScan(): Scan =
    Scan(
        id = id,
        scannedAt = scannedAt,
        codes = codes.map { code ->
            code.toScannedCode()
        },
        isFavorite = isFavorite,
    )

internal fun StoredCode.toScannedCode(): ScannedCode =
    ScannedCode(
        id = id,
        scanId = scanId,
        rawValue = rawValue,
        content = BarcodeContentParser.parse(rawValue = rawValue),
        format = BarcodeFormat.entries.firstOrNull { it.name == format },
        scannedAt = scannedAt,
        isFavorite = isScanFavorite,
        isInGroup = scanCodeCount > 1,
    )