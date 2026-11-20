package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.domain.mapper.toScan
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.normalized
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

interface RecordScanUseCase {

    /**
     * Records the [barcodes] read in one capture, at least one, and returns the capture. The same code
     * read several times is recorded once, at its first reading.
     */
    suspend operator fun invoke(barcodes: List<DetectedBarcode>): Scan
}

internal class RecordScanUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val clock: Clock,
) : RecordScanUseCase {

    override suspend fun invoke(barcodes: List<DetectedBarcode>): Scan {
        require(barcodes.isNotEmpty()) { "A scan holds at least one code" }

        val codes = barcodes
            .map { barcode ->
                barcode.normalized()
            }
            .distinct()
            .map { barcode ->
                CodeToSave(
                    rawValue = barcode.rawValue,
                    format = barcode.format.name,
                )
            }

        val scanId = scanRepository.saveScan(
            codes = codes,
            scannedAt = clock.now(),
        )

        // Read back, for the ids of the codes the database just gave
        return checkNotNull(scanRepository.watchScan(id = scanId).first()) {
            "The scan $scanId was not found once recorded"
        }.toScan()
    }
}