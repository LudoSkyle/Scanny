package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentSizeMapperTest {

    @Test
    fun `digits only are counted as digits`() {
        // When
        val size = qrContentSize(
            rawValue = "0123456789",
            errorCorrection = QrErrorCorrection.H,
        )

        // Then
        assertEquals(
            expected = ContentSize(
                used = 10,
                max = 3057,
                hasWideCharacters = false,
            ),
            actual = size,
        )
    }

    @Test
    fun `upper case letters and digits are counted as characters`() {
        // When
        val size = qrContentSize(
            rawValue = "SCANNY 2.0",
            errorCorrection = QrErrorCorrection.H,
        )

        // Then
        assertEquals(
            expected = ContentSize(
                used = 10,
                max = 1852,
                hasWideCharacters = false,
            ),
            actual = size,
        )
    }

    @Test
    fun `any other text is counted in bytes where an accent or an emoji takes several`() {
        // When
        val size = qrContentSize(
            rawValue = "Café ☕",
            errorCorrection = QrErrorCorrection.H,
        )

        // Then
        // "Caf" 3, "é" 2, " " 1, "☕" 3
        assertEquals(
            expected = ContentSize(
                used = 9,
                max = 1273,
                hasWideCharacters = true,
            ),
            actual = size,
        )
    }

    @Test
    fun `a text of single bytes has no wide character`() {
        // When
        val size = qrContentSize(
            rawValue = "https://scanny.app",
            errorCorrection = QrErrorCorrection.H,
        )

        // Then
        assertFalse(size.hasWideCharacters)
    }

    @Test
    fun `the most counted is the most the encoder holds in each mode at each correction`() {
        val units = mapOf(
            "digits" to "7",
            "characters" to "A",
            "bytes" to "a",
            "wide bytes" to "é",
        )
        QrErrorCorrection.entries.forEach { errorCorrection ->
            units.forEach { (name, unit) ->
                // Given
                val max = qrContentSize(rawValue = unit, errorCorrection = errorCorrection).max
                // As many as the most counted holds, then one more
                val fullest = unit.repeat(max / unit.encodeToByteArray().size)
                val tooLong = fullest + unit

                // Then
                assertTrue(
                    BarcodeFormat.QR_CODE.canHold(rawValue = fullest, errorCorrection = errorCorrection),
                    "$max $name at $errorCorrection should be held",
                )
                assertFalse(
                    qrContentSize(rawValue = fullest, errorCorrection = errorCorrection).isOverLimit,
                    "$max $name at $errorCorrection should be within the limit",
                )
                assertFalse(
                    BarcodeFormat.QR_CODE.canHold(rawValue = tooLong, errorCorrection = errorCorrection),
                    "One more than $max $name at $errorCorrection should be refused",
                )
                assertTrue(
                    qrContentSize(rawValue = tooLong, errorCorrection = errorCorrection).isOverLimit,
                    "One more than $max $name at $errorCorrection should be over the limit",
                )
            }
        }
    }
}
