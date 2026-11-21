package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.model.CodeReadability
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CodeStyleMapperTest {

    @Test
    fun `a qr code with a logo is encoded with the highest correction`() {
        // Given
        val style = CodeStyle(logo = byteArrayOf(1, 2, 3))

        // When
        val options = style.toQrOptions()

        // Then
        assertEquals(
            expected = QrErrorCorrectionLevel.High to QrErrorCorrection.H,
            actual = options.errorCorrectionLevel to style.qrErrorCorrection(),
        )
    }

    @Test
    fun `a qr code without a logo is encoded with the lowest correction`() {
        // Given
        val style = CodeStyle()

        // When
        val options = style.toQrOptions()

        // Then
        assertEquals(
            expected = QrErrorCorrectionLevel.Auto to QrErrorCorrection.L,
            actual = options.errorCorrectionLevel to style.qrErrorCorrection(),
        )
    }

    @Test
    fun `dark dots on a light background read well`() {
        // When
        val readability = CodeStyle(
            darkColor = 0xFF000000,
            lightColor = 0xFFFFFFFF,
        ).readability()

        // Then
        assertEquals(
            expected = CodeReadability.GOOD to 21.0,
            actual = readability to contrastRatio(0xFF000000, 0xFFFFFFFF),
        )
    }

    @Test
    fun `grey dots on a white background stand out too little`() {
        // When
        val readability = CodeStyle(
            darkColor = 0xFFAAAAAA,
            lightColor = 0xFFFFFFFF,
        ).readability()

        // Then
        assertEquals(
            expected = CodeReadability.LOW_CONTRAST,
            actual = readability,
        )
    }

    @Test
    fun `light dots on a dark background are inverted however contrasted`() {
        // When
        val readability = CodeStyle(
            darkColor = 0xFFFFFFFF,
            lightColor = 0xFF000000,
        ).readability()

        // Then
        assertEquals(
            expected = CodeReadability.INVERTED,
            actual = readability,
        )
    }

    @Test
    fun `a logo that cannot be read is left out of the code`() {
        // When
        val options = CodeStyle(logo = byteArrayOf(1, 2, 3)).toQrOptions()

        // Then
        assertNull(options.logo.painter)
    }
}
