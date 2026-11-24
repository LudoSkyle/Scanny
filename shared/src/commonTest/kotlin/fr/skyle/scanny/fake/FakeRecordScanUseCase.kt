package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * Records the scans it is given, their codes as a [content] chosen by the test, or fails if [error] is
 * set. With a [completion], a recording stays in progress until the test completes it.
 */
internal class FakeRecordScanUseCase(
    private val content: (rawValue: String) -> BarcodeContent = { rawValue -> BarcodeContent.Text(text = rawValue) },
    private val error: Exception? = null,
    private val completion: CompletableDeferred<Unit>? = null,
) : RecordScanUseCase {

    val recordedScans = mutableListOf<List<DetectedBarcode>>()

    private var lastCodeId = 0L

    override suspend fun invoke(barcodes: List<DetectedBarcode>): Scan {
        error?.let { throw it }

        recordedScans += barcodes
        completion?.await()

        val scanId = recordedScans.size.toLong()
        return Scan(
            id = scanId,
            scannedAt = SCANNED_AT,
            codes = barcodes.map { barcode ->
                lastCodeId += 1
                ScannedCode(
                    id = lastCodeId,
                    scanId = scanId,
                    rawValue = barcode.rawValue,
                    content = content(barcode.rawValue),
                    format = barcode.format,
                    scannedAt = SCANNED_AT,
                    isFavorite = false,
                    isInGroup = false,
                )
            },
            isFavorite = false,
        )
    }

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(0)
    }
}