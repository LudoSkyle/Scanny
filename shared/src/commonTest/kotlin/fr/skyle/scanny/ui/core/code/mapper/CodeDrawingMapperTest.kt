package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import io.github.alexzhirkevich.qrose.oned.BarcodeType
import kotlin.test.Test
import kotlin.test.assertEquals

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
}