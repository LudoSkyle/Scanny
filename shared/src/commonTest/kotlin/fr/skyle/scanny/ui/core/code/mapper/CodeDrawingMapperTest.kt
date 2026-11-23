package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.oned.BarcodeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodeDrawingMapperTest {

    @Test
    fun `every format is drawn as a qr code or a barcode or from a matrix`() {
        // When
        val undrawnFormats = BarcodeFormat.entries.filter { format ->
            format != BarcodeFormat.QR_CODE && format.toBarcodeType() == null && format.toMatrixEncoder() == null
        }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = undrawnFormats,
        )
    }

    @Test
    fun `only the codes in two dimensions other than the qr code are drawn from a matrix`() {
        // When
        val matrixFormats = BarcodeFormat.entries.filter { format -> format.toMatrixEncoder() != null }

        // Then
        assertEquals(
            expected = listOf(BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF417),
            actual = matrixFormats,
        )
    }

    @Test
    fun `each barcode is drawn as the barcode of its format`() {
        // When
        val barcodeTypes = BarcodeFormat.entries.associateWith { format -> format.toBarcodeType() }

        // Then
        assertEquals(
            expected = mapOf(
                BarcodeFormat.QR_CODE to null,
                BarcodeFormat.DATA_MATRIX to null,
                BarcodeFormat.AZTEC to null,
                BarcodeFormat.PDF417 to null,
                BarcodeFormat.EAN_13 to BarcodeType.EAN13,
                BarcodeFormat.EAN_8 to BarcodeType.EAN8,
                BarcodeFormat.UPC_A to BarcodeType.UPCA,
                BarcodeFormat.UPC_E to BarcodeType.UPCE,
                BarcodeFormat.CODE_39 to BarcodeType.Code39,
                BarcodeFormat.CODE_93 to BarcodeType.Code93,
                BarcodeFormat.CODE_128 to BarcodeType.Code128,
                BarcodeFormat.ITF to BarcodeType.ITF,
                BarcodeFormat.CODABAR to BarcodeType.Codabar,
            ),
            actual = barcodeTypes,
        )
    }

    @Test
    fun `a barcode holds a value its format accepts and refuses a wrong check digit`() {
        // Then
        assertEquals(
            expected = listOf(true, false, true, false),
            actual = listOf(
                BarcodeFormat.EAN_13.canHold(rawValue = "4006381333931"),
                BarcodeFormat.EAN_13.canHold(rawValue = "4006381333932"),
                BarcodeFormat.CODE_128.canHold(rawValue = "SCANNY-128"),
                BarcodeFormat.EAN_8.canHold(rawValue = "letters"),
            ),
        )
    }

    @Test
    fun `a qr code refuses a text too long for the highest correction`() {
        // Given
        val longText = "a".repeat(2_000)

        // Then
        assertTrue(BarcodeFormat.QR_CODE.canHold(rawValue = longText))
        assertFalse(
            BarcodeFormat.QR_CODE.canHold(
                rawValue = longText,
                errorCorrection = QrErrorCorrection.H,
            ),
        )
    }

    @Test
    fun `a code in two dimensions holds a text and refuses one too long for it`() {
        // Given
        // Longer than the largest Data Matrix, Aztec code and PDF417 hold
        val longText = "a".repeat(4_000)

        // Then
        assertEquals(
            expected = listOf(true, true, true, false, false, false),
            actual = listOf(
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = "Hello"),
                BarcodeFormat.AZTEC.canHold(rawValue = "Hello"),
                BarcodeFormat.PDF417.canHold(rawValue = "Hello"),
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = longText),
                BarcodeFormat.AZTEC.canHold(rawValue = longText),
                BarcodeFormat.PDF417.canHold(rawValue = longText),
            ),
        )
    }

    @Test
    fun `a data matrix and an aztec code refuse a character beyond latin-1 that a pdf417 holds`() {
        // Then
        assertEquals(
            expected = listOf(true, true, false, false, true),
            actual = listOf(
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = "Café à l'été"),
                BarcodeFormat.AZTEC.canHold(rawValue = "Café à l'été"),
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = "Prix : 5 €"),
                BarcodeFormat.AZTEC.canHold(rawValue = "Prix : 5 €"),
                BarcodeFormat.PDF417.canHold(rawValue = "Prix : 5 €"),
            ),
        )
    }

    @Test
    fun `an empty text holds nothing`() {
        // Then
        assertEquals(
            expected = listOf(false, false, false),
            actual = listOf(
                BarcodeFormat.QR_CODE.canHold(rawValue = ""),
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = ""),
                BarcodeFormat.EAN_13.canHold(rawValue = ""),
            ),
        )
    }
}