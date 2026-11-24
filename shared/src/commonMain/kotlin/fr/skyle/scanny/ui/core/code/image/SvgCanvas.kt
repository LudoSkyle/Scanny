package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.alexzhirkevich.qrose.ImageFormat
import io.github.alexzhirkevich.qrose.toByteArray
import kotlin.io.encoding.Base64
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * A canvas that writes what is drawn on it as the elements of an SVG image, for the painters of the
 * codes to draw a vector image as they draw on the screen. Its coordinates are those of the image:
 * each path is written already moved by the transformations of the canvas.
 *
 * A paint with a shader, a gradient, is filled with [shaderFill], the reference of a gradient the
 * image defines: the shader of a paint cannot be read back. A path drawn with [BlendMode.Clear], the
 * blank qrose clears around a logo, hides what was drawn before it.
 */
internal class SvgCanvas(
    private val shaderFill: String? = null,
) : Canvas {

    private class State(
        val matrix: Matrix,
        val clipId: String?,
    )

    private val definitions = StringBuilder()
    private var elements = StringBuilder()
    private var nextId = 0

    private var matrix = Matrix()
    private var clipId: String? = null
    private val savedStates = ArrayDeque<State>()

    /** The definitions the elements refer to, clips and masks, then the elements, in drawing order. */
    fun content(): String =
        buildString {
            if (definitions.isNotEmpty()) {
                append("<defs>").append(definitions).append("</defs>")
            }
            append(elements)
        }

    override fun save() {
        savedStates.addLast(State(matrix = matrix.copyOf(), clipId = clipId))
    }

    override fun restore() {
        val state = savedStates.removeLastOrNull() ?: return
        matrix = state.matrix
        clipId = state.clipId
    }

    // A layer only matters for a blending this canvas does not do: its content is drawn as is
    override fun saveLayer(bounds: Rect, paint: Paint) {
        save()
    }

    override fun translate(dx: Float, dy: Float) {
        concat(Matrix().apply { translate(x = dx, y = dy) })
    }

    override fun scale(sx: Float, sy: Float) {
        concat(Matrix().apply { scale(x = sx, y = sy) })
    }

    override fun rotate(degrees: Float) {
        concat(Matrix().apply { rotateZ(degrees = degrees) })
    }

    override fun skew(sx: Float, sy: Float) {
        concat(
            Matrix().apply {
                this[1, 0] = sx
                this[0, 1] = sy
            },
        )
    }

    // The transformation applies before those of the canvas, as on any canvas
    override fun concat(matrix: Matrix) {
        val combined = matrix.copyOf()
        combined *= this.matrix
        this.matrix = combined
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float, clipOp: ClipOp) {
        clipPath(
            path = Path().apply { addRect(rect = Rect(left = left, top = top, right = right, bottom = bottom)) },
            clipOp = clipOp,
        )
    }

    // A clip within a clip is the intersection of both: the inner one is itself clipped by the outer
    override fun clipPath(path: Path, clipOp: ClipOp) {
        require(clipOp == ClipOp.Intersect) { "An SVG image only intersects its clips" }
        val id = newId(prefix = "clip")
        definitions.append("<clipPath id=\"").append(id).append('"')
        clipId?.let { parent -> definitions.append(" clip-path=\"url(#").append(parent).append(")\"") }
        definitions.append("><path d=\"").append(path.toSvgData()).append('"')
        definitions.append(fillRuleOf(path)).append("/></clipPath>")
        clipId = id
    }

    override fun drawLine(p1: Offset, p2: Offset, paint: Paint) {
        drawPath(
            path = Path().apply {
                moveTo(x = p1.x, y = p1.y)
                lineTo(x = p2.x, y = p2.y)
            },
            paint = paint,
        )
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        drawPath(
            path = Path().apply { addRect(rect = Rect(left = left, top = top, right = right, bottom = bottom)) },
            paint = paint,
        )
    }

    override fun drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radiusX: Float,
        radiusY: Float,
        paint: Paint,
    ) {
        drawPath(
            path = Path().apply {
                addRoundRect(
                    roundRect = RoundRect(
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                        radiusX = radiusX,
                        radiusY = radiusY,
                    ),
                )
            },
            paint = paint,
        )
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        drawPath(
            path = Path().apply { addOval(oval = Rect(left = left, top = top, right = right, bottom = bottom)) },
            paint = paint,
        )
    }

    override fun drawCircle(center: Offset, radius: Float, paint: Paint) {
        drawPath(
            path = Path().apply { addOval(oval = Rect(center = center, radius = radius)) },
            paint = paint,
        )
    }

    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        paint: Paint,
    ) {
        val oval = Rect(left = left, top = top, right = right, bottom = bottom)
        drawPath(
            path = Path().apply {
                if (useCenter) {
                    moveTo(x = oval.center.x, y = oval.center.y)
                }
                arcTo(
                    rect = oval,
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = sweepAngle,
                    forceMoveTo = !useCenter,
                )
                if (useCenter) {
                    close()
                }
            },
            paint = paint,
        )
    }

    override fun drawPath(path: Path, paint: Paint) {
        if (paint.blendMode == BlendMode.Clear) {
            clearUnder(path = path)
            return
        }

        elements.append("<path d=\"").append(path.toSvgData()).append('"')
        elements.append(fillRuleOf(path)).append(paintAttributes(paint)).append(clipAttribute()).append("/>")
    }

    override fun drawImage(image: ImageBitmap, topLeftOffset: Offset, paint: Paint) {
        save()
        translate(dx = topLeftOffset.x, dy = topLeftOffset.y)
        drawImageRect(image = image, paint = paint)
        restore()
    }

    // Embedded as a PNG, stretched from its part [srcOffset, srcSize] to [dstOffset, dstSize]
    override fun drawImageRect(
        image: ImageBitmap,
        srcOffset: IntOffset,
        srcSize: IntSize,
        dstOffset: IntOffset,
        dstSize: IntSize,
        paint: Paint,
    ) {
        val png = Base64.encode(image.toByteArray(format = ImageFormat.PNG))
        // The clip in the coordinates of the image, outside the transformation of the canvas
        elements.append("<g").append(clipAttribute()).append("><g transform=\"").append(transformAttribute())
            .append("\">")
        val isWhole = srcOffset == IntOffset.Zero && srcSize == IntSize(width = image.width, height = image.height)
        if (isWhole) {
            appendImage(png = png, x = dstOffset.x, y = dstOffset.y, width = dstSize.width, height = dstSize.height)
        } else {
            // A part of the image only: a viewport of its size shows that part
            elements.append("<svg x=\"").append(dstOffset.x).append("\" y=\"").append(dstOffset.y)
                .append("\" width=\"").append(dstSize.width).append("\" height=\"").append(dstSize.height)
                .append("\" viewBox=\"").append(srcOffset.x).append(' ').append(srcOffset.y).append(' ')
                .append(srcSize.width).append(' ').append(srcSize.height)
                .append("\" preserveAspectRatio=\"none\" overflow=\"hidden\">")
            appendImage(png = png, x = 0, y = 0, width = image.width, height = image.height)
            elements.append("</svg>")
        }
        elements.append("</g></g>")
    }

    // Both references to the image: href, of SVG 2, and xlink:href, the only one older editors read
    private fun appendImage(png: String, x: Int, y: Int, width: Int, height: Int) {
        elements.append("<image x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(width)
            .append("\" height=\"").append(height).append("\" preserveAspectRatio=\"none\" href=\"data:image/png;base64,")
            .append(png).append("\" xlink:href=\"data:image/png;base64,").append(png).append("\"/>")
    }

    override fun drawPoints(pointMode: PointMode, points: List<Offset>, paint: Paint) {
        error("No painter of a code draws points")
    }

    override fun drawRawPoints(pointMode: PointMode, points: FloatArray, paint: Paint) {
        error("No painter of a code draws points")
    }

    override fun drawVertices(vertices: Vertices, blendMode: BlendMode, paint: Paint) {
        error("No painter of a code draws vertices")
    }

    override fun enableZ() = Unit

    override fun disableZ() = Unit

    // The elements drawn so far go under a mask, black under the path: they no longer show there
    private fun clearUnder(path: Path) {
        val id = newId(prefix = "clear")
        definitions.append("<mask id=\"").append(id).append("\" maskUnits=\"userSpaceOnUse\"")
            .append(" x=\"-100%\" y=\"-100%\" width=\"300%\" height=\"300%\">")
            .append("<rect x=\"-100%\" y=\"-100%\" width=\"300%\" height=\"300%\" fill=\"#fff\"/>")
            .append("<path d=\"").append(path.toSvgData()).append('"').append(fillRuleOf(path))
            .append(clipAttribute()).append(" fill=\"#000\"/></mask>")
        elements = StringBuilder().append("<g mask=\"url(#").append(id).append(")\">").append(elements).append("</g>")
    }

    private fun Path.toSvgData(): String =
        buildString {
            val points = FloatArray(8)
            val segments = this@toSvgData.iterator()
            while (segments.hasNext()) {
                val type = segments.next(outPoints = points)
                when (type) {
                    PathSegment.Type.Move -> appendCommand(command = 'M', points = points, from = 0, count = 1)
                    PathSegment.Type.Line -> appendCommand(command = 'L', points = points, from = 1, count = 1)
                    PathSegment.Type.Quadratic -> appendCommand(command = 'Q', points = points, from = 1, count = 2)
                    PathSegment.Type.Cubic -> appendCommand(command = 'C', points = points, from = 1, count = 3)
                    PathSegment.Type.Conic -> appendConic(points = points)
                    PathSegment.Type.Close -> append('Z')
                    PathSegment.Type.Done -> Unit
                }
            }
        }

    /**
     * A conic, the arc of an oval or of a rounded corner, as SVG has none: the cubic that leaves and
     * reaches it in the same directions, which a circle's quarter matches to the eye. Its weight follows
     * its three points.
     */
    private fun StringBuilder.appendConic(points: FloatArray) {
        val weight = points[6]
        val factor = 4 * weight / (3 * (1 + weight))
        val cubic = floatArrayOf(
            points[0],
            points[1],
            points[0] + factor * (points[2] - points[0]),
            points[1] + factor * (points[3] - points[1]),
            points[4] + factor * (points[2] - points[4]),
            points[5] + factor * (points[3] - points[5]),
            points[4],
            points[5],
        )
        appendCommand(command = 'C', points = cubic, from = 1, count = 3)
    }

    // The points of a segment after its start point, the end of the previous one, moved by the canvas
    private fun StringBuilder.appendCommand(command: Char, points: FloatArray, from: Int, count: Int) {
        append(command)
        for (index in from until from + count) {
            val point = matrix.map(Offset(x = points[2 * index], y = points[2 * index + 1]))
            if (index > from) {
                append(' ')
            }
            append(format(point.x)).append(',').append(format(point.y))
        }
    }

    private fun fillRuleOf(path: Path): String =
        if (path.fillType == PathFillType.EvenOdd) " fill-rule=\"evenodd\"" else ""

    private fun paintAttributes(paint: Paint): String =
        buildString {
            val fill = shaderFill.takeIf { paint.shader != null } ?: paint.color.toSvgColor()
            val opacity = if (paint.shader != null) paint.alpha else paint.color.alpha
            if (paint.style == PaintingStyle.Stroke) {
                append(" fill=\"none\" stroke=\"").append(fill).append("\" stroke-width=\"")
                    .append(format(paint.strokeWidth * scaleOf(matrix))).append('"')
                append(" stroke-linecap=\"").append(paint.strokeCap.toSvgName()).append('"')
                append(" stroke-linejoin=\"").append(paint.strokeJoin.toSvgName()).append('"')
                if (opacity < 1f) {
                    append(" stroke-opacity=\"").append(format(opacity)).append('"')
                }
            } else {
                append(" fill=\"").append(fill).append('"')
                if (opacity < 1f) {
                    append(" fill-opacity=\"").append(format(opacity)).append('"')
                }
            }
        }

    private fun clipAttribute(): String =
        clipId?.let { id -> " clip-path=\"url(#$id)\"" }.orEmpty()

    // The 2D part of the matrix of the canvas: a b c d e f, as SVG writes it
    private fun transformAttribute(): String =
        "matrix(${format(matrix[0, 0])} ${format(matrix[0, 1])} ${format(matrix[1, 0])} " +
            "${format(matrix[1, 1])} ${format(matrix[3, 0])} ${format(matrix[3, 1])})"

    private fun newId(prefix: String): String =
        "$prefix${nextId++}"
}

private fun Matrix.copyOf(): Matrix =
    Matrix(values = values.copyOf())

// How much a stroke's width grows through a transformation: the square root of its area's scale
private fun scaleOf(matrix: Matrix): Float =
    sqrt(abs(matrix[0, 0] * matrix[1, 1] - matrix[0, 1] * matrix[1, 0]))

private fun StrokeCap.toSvgName(): String =
    when (this) {
        StrokeCap.Round -> "round"
        StrokeCap.Square -> "square"
        else -> "butt"
    }

private fun StrokeJoin.toSvgName(): String =
    when (this) {
        StrokeJoin.Round -> "round"
        StrokeJoin.Bevel -> "bevel"
        else -> "miter"
    }

/** The color as SVG writes it, `#RRGGBB`, its opacity apart. */
internal fun androidx.compose.ui.graphics.Color.toSvgColor(): String {
    fun channel(value: Float): String = (value * 255).roundToLong().toString(radix = 16).padStart(length = 2, padChar = '0')
    return "#${channel(red)}${channel(green)}${channel(blue)}"
}

/** A coordinate to the hundredth, without a useless zero: SVG reads no exponent such as `1.0E-5`. */
internal fun format(value: Float): String {
    val hundredths = (value * 100).roundToLong()
    val sign = if (hundredths < 0) "-" else ""
    val absolute = abs(hundredths)
    val units = absolute / 100
    val decimals = absolute % 100
    return when {
        decimals == 0L -> "$sign$units"
        decimals % 10 == 0L -> "$sign$units.${decimals / 10}"
        else -> "$sign$units.${decimals.toString().padStart(length = 2, padChar = '0')}"
    }.let { text -> if (text == "-0") "0" else text }
}
