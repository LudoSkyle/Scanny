package fr.skyle.scanny.ui.core.code.mapper

import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.domain.model.CodeErrorCorrection
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
    fun `a qr code without a logo left to scanny is encoded with the lowest correction`() {
        // Given
        val style = CodeStyle()

        // When
        val options = style.toQrOptions()

        // Then
        assertEquals(
            expected = QrErrorCorrectionLevel.Low to QrErrorCorrection.L,
            actual = options.errorCorrectionLevel to style.qrErrorCorrection(),
        )
    }

    @Test
    fun `a qr code is encoded with the correction chosen`() {
        // When
        val corrections = CodeErrorCorrection.entries.associateWith { errorCorrection ->
            val style = CodeStyle(errorCorrection = errorCorrection)
            style.toQrOptions().errorCorrectionLevel to style.qrErrorCorrection()
        }

        // Then
        assertEquals(
            expected = mapOf(
                CodeErrorCorrection.AUTO to (QrErrorCorrectionLevel.Low to QrErrorCorrection.L),
                CodeErrorCorrection.LOW to (QrErrorCorrectionLevel.Low to QrErrorCorrection.L),
                CodeErrorCorrection.MEDIUM to (QrErrorCorrectionLevel.Medium to QrErrorCorrection.M),
                CodeErrorCorrection.QUARTILE to (QrErrorCorrectionLevel.MediumHigh to QrErrorCorrection.Q),
                CodeErrorCorrection.HIGH to (QrErrorCorrectionLevel.High to QrErrorCorrection.H),
            ),
            actual = corrections,
        )
    }

    @Test
    fun `a logo is always encoded with the highest correction whatever the one stored`() {
        // Given
        val style = CodeStyle(
            logo = byteArrayOf(1, 2, 3),
            errorCorrection = CodeErrorCorrection.LOW,
        )

        // When
        val options = style.toQrOptions()

        // Then
        assertEquals(
            expected = QrErrorCorrectionLevel.High to QrErrorCorrection.H,
            actual = options.errorCorrectionLevel to style.qrErrorCorrection(),
        )
    }

    @Test
    fun `the corners are drawn in their own color or in the one of the dots`() {
        // When
        val colors = listOf(
            CodeStyle(darkColor = 0xFF3B43BE),
            CodeStyle(darkColor = 0xFF3B43BE, cornerColor = 0xFFB71C1C),
        ).map { style -> style.cornerColor() }

        // Then
        assertEquals(
            expected = listOf(Color(0xFF3B43BE), Color(0xFFB71C1C)),
            actual = colors,
        )
    }

    @Test
    fun `corners that stand out too little from the background are told`() {
        // When
        val readability = CodeStyle(cornerColor = 0xFFDDDDDD).readability()

        // Then
        assertEquals(
            expected = CodeReadability.LOW_CONTRAST,
            actual = readability,
        )
    }

    @Test
    fun `corners lighter than the background are told as inverted`() {
        // When
        val readability = CodeStyle(
            lightColor = 0xFF3E2723,
            darkColor = 0xFF000000,
            cornerColor = 0xFFFFFFFF,
        ).readability()

        // Then
        assertEquals(
            expected = CodeReadability.INVERTED,
            actual = readability,
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
