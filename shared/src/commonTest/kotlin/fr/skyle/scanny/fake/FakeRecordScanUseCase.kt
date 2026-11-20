package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * Records the scans it is given, as a [content] chosen by the test, or fails if [error] is set. With a
 * [completion], a recording stays in progress until the test completes it.
 */
internal class FakeRecordScanUseCase(
    private val content: (rawValue: String) -> BarcodeContent = { rawValue -> BarcodeContent.Text(text = rawValue) },
    private val error: Exception? = null,
    private val completion: CompletableDeferred<Unit>? = null,
) : RecordScanUseCase {

    val recordedBarcodes = mutableListOf<Pair<String, BarcodeFormat>>()

    override suspend fun invoke(
        rawValue: String,
        format: BarcodeFormat,
    ): Scan {
        error?.let { throw it }

        recordedBarcodes += rawValue to format
        completion?.await()
        return Scan(
            id = recordedBarcodes.size.toLong(),
            rawValue = rawValue,
            content = content(rawValue),
            format = format,
            scannedAt = Instant.fromEpochMilliseconds(0),
        )
    }
}