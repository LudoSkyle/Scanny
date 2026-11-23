package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.mapper.toScan
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.normalized
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

interface RecordScanUseCase {

    /**
     * Records the [barcodes] read in one capture, at least one, and returns the capture. The same code
     * read several times is recorded once, at its first reading.
     *
     * When the user keeps no history, the capture is recorded out of it: shown once, then purged. When
     * the user skips the consecutive duplicates, the same codes as the newest capture of the history,
     * recorded a moment ago, give that capture the new time instead of making a new one.
     */
    suspend operator fun invoke(barcodes: List<DetectedBarcode>): Scan
}

internal class RecordScanUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
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

        val now = clock.now()
        val isScanHistoryEnabled = settingsRepository.watchIsScanHistoryEnabled().first()

        if (isScanHistoryEnabled && settingsRepository.watchIsConsecutiveDuplicateSkipped().first()) {
            val latestScan = scanRepository.findLatestScan()
            // The same codes, in any order: a multiple scan reads them as they come
            val isDuplicate = latestScan != null &&
                now - latestScan.scannedAt <= CONSECUTIVE_DUPLICATE_WINDOW &&
                latestScan.codes.map { code -> code.rawValue to code.format }.toSet() ==
                codes.map { code -> code.rawValue to code.format }.toSet()
            if (latestScan != null && isDuplicate) {
                scanRepository.updateScanTime(
                    id = latestScan.id,
                    scannedAt = now,
                )
                return readScan(scanId = latestScan.id)
            }
        }

        val scanId = scanRepository.saveScan(
            codes = codes,
            scannedAt = now,
            isInHistory = isScanHistoryEnabled,
        )
        return readScan(scanId = scanId)
    }

    // Read back, for the ids of the codes the database gave
    private suspend fun readScan(scanId: Long): Scan =
        checkNotNull(scanRepository.watchScan(id = scanId).first()) {
            "The scan $scanId was not found once recorded"
        }.toScan()

    internal companion object {
        val CONSECUTIVE_DUPLICATE_WINDOW = 5.minutes
    }
}