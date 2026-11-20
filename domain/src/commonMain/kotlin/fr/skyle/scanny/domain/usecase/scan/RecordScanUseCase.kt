package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.payload.BarcodeContentParser
import kotlin.time.Clock

interface RecordScanUseCase {

    suspend operator fun invoke(
        rawValue: String,
        format: BarcodeFormat,
    ): Scan
}

internal class RecordScanUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val clock: Clock,
) : RecordScanUseCase {

    override suspend fun invoke(
        rawValue: String,
        format: BarcodeFormat,
    ): Scan {
        val scannedAt = clock.now()

        val id = scanRepository.saveScan(
            rawValue = rawValue,
            format = format.name,
            scannedAt = scannedAt,
        )

        return Scan(
            id = id,
            rawValue = rawValue,
            content = BarcodeContentParser.parse(rawValue = rawValue),
            format = format,
            scannedAt = scannedAt,
        )
    }
}