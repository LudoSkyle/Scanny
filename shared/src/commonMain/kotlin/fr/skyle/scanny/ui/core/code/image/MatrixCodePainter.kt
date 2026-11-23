package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.matrixRowHeight
import fr.skyle.scanny.ui.core.code.mapper.toMatrixEncoder
import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A code in two dimensions drawn from its modules in [color], each row [rowHeight] modules high: qrose
 * encodes a Data Matrix, an Aztec code or a PDF417, but draws none of them. Its blank margin, the quiet
 * zone, is left to whoever lays it out.
 */
internal class MatrixCodePainter(
    private val matrix: Matrix2D,
    private val rowHeight: Int,
    private val color: Color,
) : Painter() {

    override val intrinsicSize: Size = Size(width = matrix.width.toFloat(), height = (matrix.height * rowHeight).toFloat())

    override fun DrawScope.onDraw() {
        val module = min(size.width / matrix.width, size.height / (matrix.height * rowHeight))
        val left = (size.width - module * matrix.width) / 2
        val top = (size.height - module * matrix.height * rowHeight) / 2

        // Each edge on a whole pixel, the nearest: a scanner reads sharp edges better, and the code keeps
        // its size
        fun pixelX(x: Int): Float = (left + x * module).roundToInt().toFloat()
        fun pixelY(y: Int): Float = (top + y * rowHeight * module).roundToInt().toFloat()

        for (y in 0 until matrix.height) {
            // A run of dark modules drawn as one rectangle: no seam between them
            var x = 0
            while (x < matrix.width) {
                if (!matrix[x, y]) {
                    x++
                    continue
                }
                val start = x
                while (x < matrix.width && matrix[x, y]) {
                    x++
                }
                drawRect(
                    color = color,
                    topLeft = Offset(x = pixelX(start), y = pixelY(y)),
                    size = Size(width = pixelX(x) - pixelX(start), height = pixelY(y + 1) - pixelY(y)),
                )
            }
        }
    }
}

/**
 * The code holding [rawValue] in [format], a format [toMatrixEncoder] encodes, drawn in [color]. Throws
 * for a text its format refuses.
 */
internal fun MatrixCodePainter(
    rawValue: String,
    format: BarcodeFormat,
    color: Color,
): MatrixCodePainter {
    val encoder = requireNotNull(format.toMatrixEncoder()) { "qrose draws $format itself" }
    return MatrixCodePainter(
        matrix = encoder.encode(rawValue),
        rowHeight = format.matrixRowHeight,
        color = color,
    )
}
