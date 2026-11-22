package fr.skyle.scanny.ui.core.code.encoder

import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.Reader
import com.google.zxing.aztec.AztecReader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.datamatrix.DataMatrixReader
import com.google.zxing.pdf417.PDF417Reader
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.matrixRowHeight
import fr.skyle.scanny.ui.core.code.mapper.toMatrixEncoder
import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The encoders of the codes in two dimensions qrose does not draw, read back by ZXing from their
 * modules: qrose's encoders of Data Matrix and Aztec wrote one code in ten or so wrong, with no error.
 * Random texts, always the same ones, of every kind of character the formats hold.
 */
class MatrixEncoderReadBackTest {

    private val random = Random(seed = 15_438)

    @Test
    fun `random texts of every alphabet are read back in a data matrix`() {
        assertReadBack(format = BarcodeFormat.DATA_MATRIX)
    }

    @Test
    fun `random texts of every alphabet are read back in an aztec code`() {
        assertReadBack(format = BarcodeFormat.AZTEC)
    }

    @Test
    fun `random texts of every alphabet are read back in a pdf417`() {
        assertReadBack(format = BarcodeFormat.PDF417)
    }

    @Test
    fun `the largest data matrix whose error correction starts at its ninth block is read back`() {
        // Given
        // Enough for the 144 × 144 symbol, the one whose blocks are not of the same length
        val text = randomText(
            alphabet = PRINTABLE_ASCII,
            length = 1_500,
        )

        // When
        val read = BarcodeFormat.DATA_MATRIX.readBack(text = text)

        // Then
        assertEquals(
            expected = text,
            actual = read,
        )
    }

    @Test
    fun `the largest aztec code is read back`() {
        // Given
        val text = randomText(
            alphabet = PRINTABLE_ASCII,
            length = 1_800,
        )

        // When
        val read = BarcodeFormat.AZTEC.readBack(text = text)

        // Then
        assertEquals(
            expected = text,
            actual = read,
        )
    }

    private fun assertReadBack(format: BarcodeFormat) {
        // Given
        val texts = ALPHABETS.flatMap { alphabet ->
            List(TEXTS_PER_ALPHABET) {
                randomText(
                    alphabet = alphabet,
                    length = random.nextInt(from = 1, until = 300),
                )
            }
        }

        // When
        val unreadTexts = texts.filterNot { text -> format.readBack(text = text) == text }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = unreadTexts,
        )
    }

    private fun randomText(
        alphabet: String,
        length: Int,
    ): String =
        String(CharArray(length) { alphabet[random.nextInt(alphabet.length)] })

    // ZXing throws when it finds no code
    private fun BarcodeFormat.readBack(text: String): String? {
        val matrix = requireNotNull(toMatrixEncoder()).encode(text)
        val reader: Reader = when (this) {
            BarcodeFormat.DATA_MATRIX -> DataMatrixReader()
            BarcodeFormat.AZTEC -> AztecReader()
            else -> PDF417Reader()
        }
        val image = BinaryBitmap(HybridBinarizer(MatrixLuminanceSource(matrix = matrix, rowHeight = matrixRowHeight)))
        return runCatching { reader.decode(image).text }.getOrNull()
    }

    // The modules in black on white, a few pixels each, in a blank margin
    private class MatrixLuminanceSource(
        private val matrix: Matrix2D,
        private val rowHeight: Int,
    ) : LuminanceSource(
        (matrix.width + 2 * MARGIN) * SCALE,
        (matrix.height * rowHeight + 2 * MARGIN) * SCALE,
    ) {

        override fun getRow(y: Int, row: ByteArray?): ByteArray {
            val pixels = row?.takeIf { pixels -> pixels.size >= width } ?: ByteArray(width)
            for (x in 0 until width) {
                pixels[x] = luminance(x = x, y = y)
            }
            return pixels
        }

        override fun getMatrix(): ByteArray =
            ByteArray(width * height) { index -> luminance(x = index % width, y = index / width) }

        private fun luminance(x: Int, y: Int): Byte {
            val moduleX = x / SCALE - MARGIN
            val rowY = y / SCALE - MARGIN
            val isDark = moduleX in 0 until matrix.width && rowY in 0 until matrix.height * rowHeight &&
                matrix[moduleX, rowY / rowHeight]
            return if (isDark) BLACK else WHITE
        }
    }

    private companion object {
        const val TEXTS_PER_ALPHABET = 60
        const val SCALE = 3
        const val MARGIN = 4
        const val BLACK: Byte = 0
        const val WHITE: Byte = -1

        val PRINTABLE_ASCII = (' '..'~').joinToString(separator = "") + "\n"

        // Each alphabet switches the encoders between their modes in its own way
        val ALPHABETS = listOf(
            ('A'..'Z').joinToString(separator = ""),
            ('a'..'z').joinToString(separator = ""),
            ('0'..'9').joinToString(separator = ""),
            "ABCDEFabcdef0123:;/.,-+ \r\n",
            PRINTABLE_ASCII,
            "aeiouéèàçÉÀ ",
            // Every character of ISO 8859-1, control characters included
            (1..255).map { code -> code.toChar() }.joinToString(separator = ""),
        )
    }
}
