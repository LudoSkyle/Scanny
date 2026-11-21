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
    fun `a qr code and every barcode can be drawn again`() {
        // When
        val drawableFormats = BarcodeFormat.entries.filter { format -> format.isDrawable }

        // Then
        assertEquals(
            expected = BarcodeFormat.entries - setOf(BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF417),
            actual = drawableFormats,
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
    fun `an empty text or a format qrose cannot draw holds nothing`() {
        // Then
        assertEquals(
            expected = listOf(false, false),
            actual = listOf(
                BarcodeFormat.QR_CODE.canHold(rawValue = ""),
                BarcodeFormat.DATA_MATRIX.canHold(rawValue = "Hello"),
            ),
        )
    }
}