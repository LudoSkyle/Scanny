package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import kotlin.test.Test
import kotlin.test.assertEquals

class BarcodeExampleMapperTest {

    @Test
    fun `every barcode has an example its format accepts`() {
        // Given
        val barcodeFormats = BarcodeFormat.entries.filter { format -> format.toBarcodeType() != null }

        // When
        val wrongFormats = barcodeFormats.filterNot { format ->
            val example = format.exampleValue
            example != null && format.canHold(rawValue = example)
        }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = wrongFormats,
        )
    }

    @Test
    fun `a code in two dimensions has no example value`() {
        // When
        val examples = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX, BarcodeFormat.AZTEC, BarcodeFormat.PDF417)
            .map { format -> format.exampleValue }

        // Then
        assertEquals(
            expected = List(4) { null },
            actual = examples,
        )
    }
}
