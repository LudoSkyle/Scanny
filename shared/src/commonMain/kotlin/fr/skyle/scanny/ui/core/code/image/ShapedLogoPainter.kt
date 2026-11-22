package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import fr.skyle.scanny.domain.model.CodeShape
import kotlin.math.roundToInt

/**
 * A logo cut to its [shape]: qrose gives a shape to the blank around a logo only, and draws the logo
 * itself as a rectangle. [cornerRadius] is a fraction of the shorter side, for a rounded logo.
 */
internal class ShapedLogoPainter(
    private val image: ImageBitmap,
    private val shape: CodeShape,
    private val cornerRadius: Float,
) : Painter() {

    override val intrinsicSize: Size = Size(width = image.width.toFloat(), height = image.height.toFloat())

    override fun DrawScope.onDraw() {
        val bounds = Rect(offset = Offset.Zero, size = size)
        val path = Path().apply {
            when (shape) {
                CodeShape.SQUARE -> addRect(rect = bounds)

                CodeShape.ROUNDED -> addRoundRect(
                    roundRect = RoundRect(
                        rect = bounds,
                        cornerRadius = CornerRadius(x = size.minDimension * cornerRadius),
                    ),
                )

                CodeShape.CIRCLE -> addOval(oval = bounds)
            }
        }
        clipPath(path = path) {
            drawImage(
                image = image,
                dstSize = IntSize(width = size.width.roundToInt(), height = size.height.roundToInt()),
            )
        }
    }
}
