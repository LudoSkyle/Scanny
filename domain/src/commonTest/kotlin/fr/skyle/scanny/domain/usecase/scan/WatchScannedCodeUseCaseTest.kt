package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ScannedCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WatchScannedCodeUseCaseTest {

    private val scanRepository = FakeScanRepository(
        storedScans = listOf(
            StoredScan(
                id = 1,
                scannedAt = SCANNED_AT,
                codes = listOf(
                    storedCode(
                        id = 1,
                        rawValue = "Hello",
                    ),
                    storedCode(
                        id = 2,
                        rawValue = "mailto:john@doe.fr",
                    ),
                ),
            ),
        ),
    )

    private val watchScannedCodeUseCase = WatchScannedCodeUseCaseImpl(scanRepository = scanRepository)

    @Test
    fun `a code of a scan is watched with its parsed content`() = runTest {
        // When
        val scannedCode = watchScannedCodeUseCase(codeId = 2).first()

        // Then
        assertEquals(
            expected = ScannedCode(
                id = 2,
                scanId = 1,
                rawValue = "mailto:john@doe.fr",
                content = BarcodeContent.Email(address = "john@doe.fr"),
                format = BarcodeFormat.QR_CODE,
                scannedAt = SCANNED_AT,
            ),
            actual = scannedCode,
        )
    }

    @Test
    fun `a code that does not exist is watched as null`() = runTest {
        // When
        val scannedCode = watchScannedCodeUseCase(codeId = 404).first()

        // Then
        assertNull(scannedCode)
    }

    private fun storedCode(
        id: Long,
        rawValue: String,
    ): StoredCode =
        StoredCode(
            id = id,
            scanId = 1,
            rawValue = rawValue,
            format = BarcodeFormat.QR_CODE.name,
            scannedAt = SCANNED_AT,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}