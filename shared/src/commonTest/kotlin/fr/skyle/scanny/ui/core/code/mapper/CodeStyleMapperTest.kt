package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.CodeStyle
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
    fun `a logo that cannot be read is left out of the code`() {
        // When
        val options = CodeStyle(logo = byteArrayOf(1, 2, 3)).toQrOptions()

        // Then
        assertNull(options.logo.painter)
    }
}
