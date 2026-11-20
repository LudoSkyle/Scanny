package fr.skyle.scanny.platform.camera

import fr.skyle.scanny.domain.model.BarcodeFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class DetectedBarcodeTest {

    @Test
    fun `an ean 13 starting with 0 is a upc a without its leading 0`() {
        // Given
        val detectedBarcode = DetectedBarcode(
            rawValue = "0123456789012",
            format = BarcodeFormat.EAN_13,
        )

        // When
        val normalizedBarcode = detectedBarcode.normalized()

        // Then
        assertEquals(
            expected = DetectedBarcode(
                rawValue = "123456789012",
                format = BarcodeFormat.UPC_A,
            ),
            actual = normalizedBarcode,
        )
    }

    @Test
    fun `an ean 13 starting with another digit stays an ean 13`() {
        // Given
        val detectedBarcode = DetectedBarcode(
            rawValue = "3123456789012",
            format = BarcodeFormat.EAN_13,
        )

        // When
        val normalizedBarcode = detectedBarcode.normalized()

        // Then
        assertEquals(
            expected = detectedBarcode,
            actual = normalizedBarcode,
        )
    }

    @Test
    fun `another format starting with 0 is kept as is`() {
        // Given
        val detectedBarcode = DetectedBarcode(
            rawValue = "0123",
            format = BarcodeFormat.CODE_128,
        )

        // When
        val normalizedBarcode = detectedBarcode.normalized()

        // Then
        assertEquals(
            expected = detectedBarcode,
            actual = normalizedBarcode,
        )
    }
}