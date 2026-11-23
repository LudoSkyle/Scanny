package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.test.ComposeTest
import fr.skyle.scanny.ui.test.RED_PNG
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class CodeImageTest : ComposeTest() {

    @Test
    fun `a qr code is drawn in the colors of its style and its margin in the color of its background`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                Box(modifier = Modifier.testTag(TAG)) {
                    CodeImage(
                        modifier = Modifier.width(240.dp),
                        rawValue = "https://scanny.app",
                        format = BarcodeFormat.QR_CODE,
                        contentDescription = "QR code",
                        style = CodeStyle(
                            darkColor = INDIGO,
                            lightColor = CREAM,
                        ),
                    )
                }
            }
        }

        // Then
        // In the margin of 16 dp, then in the top left corner of the code, dark
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val margin = with(density) { (16.dp.toPx() / 2).toInt() }
        val corner = with(density) { (16.dp.toPx() + 2).toInt() }
        assertTrue(pixels[margin, margin].isCloseTo(Color(CREAM)), "The margin is ${pixels[margin, margin]}")
        assertTrue(pixels[corner, corner].isCloseTo(Color(INDIGO)), "The corner is ${pixels[corner, corner]}")
    }

    @Test
    fun `the corners of a qr code are drawn in their own color`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                Box(modifier = Modifier.testTag(TAG)) {
                    CodeImage(
                        modifier = Modifier.width(240.dp),
                        rawValue = "https://scanny.app",
                        format = BarcodeFormat.QR_CODE,
                        contentDescription = "QR code",
                        style = CodeStyle(
                            darkColor = INDIGO,
                            lightColor = CREAM,
                            cornerColor = RED,
                        ),
                    )
                }
            }
        }

        // Then
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val corner = with(density) { (16.dp.toPx() + 2).toInt() }
        assertTrue(pixels[corner, corner].isCloseTo(Color(RED)), "The corner is ${pixels[corner, corner]}")
    }

    @Test
    fun `a linear gradient goes from the color of the dots in the top left corner to its second color`() = runComposeUiTest {
        // When
        setContent {
            gradientCode(gradient = CodeGradient.LINEAR)
        }

        // Then
        // Halfway along the gradient, the top right corner is neither of its colors
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val (left, right, top) = cornerPositions(width = pixels.width)
        assertTrue(pixels[left, top].isCloseTo(Color(BLACK)), "The top left corner is ${pixels[left, top]}")
        assertTrue(
            !pixels[right, top].isCloseTo(Color(BLACK)) && !pixels[right, top].isCloseTo(Color(RED)),
            "The top right corner is ${pixels[right, top]}",
        )
    }

    @Test
    fun `a radial gradient reaches its second color in the corners`() = runComposeUiTest {
        // When
        setContent {
            gradientCode(gradient = CodeGradient.RADIAL)
        }

        // Then
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val (left, right, top) = cornerPositions(width = pixels.width)
        assertTrue(pixels[left, top].distanceTo(Color(RED)) < pixels[left, top].distanceTo(Color(BLACK)), "The top left corner is ${pixels[left, top]}")
        assertTrue(pixels[right, top].distanceTo(Color(RED)) < pixels[right, top].distanceTo(Color(BLACK)), "The top right corner is ${pixels[right, top]}")
    }

    @Composable
    private fun gradientCode(gradient: CodeGradient) {
        SCTheme {
            Box(modifier = Modifier.testTag(TAG)) {
                CodeImage(
                    modifier = Modifier.width(240.dp),
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    contentDescription = "QR code",
                    style = CodeStyle(
                        darkColor = BLACK,
                        gradient = gradient,
                        gradientColor = RED,
                    ),
                )
            }
        }
    }

    // Just inside the top left and the top right corners of the code, inside its margin of 16 dp
    private fun ComposeUiTest.cornerPositions(width: Int): Triple<Int, Int, Int> =
        with(density) {
            Triple(
                (16.dp.toPx() + 2).toInt(),
                (width - 16.dp.toPx() - 3).toInt(),
                (16.dp.toPx() + 2).toInt(),
            )
        }

    @Test
    fun `a square logo fills its square`() = runComposeUiTest {
        // When
        setContent {
            logoCode(shape = CodeShape.SQUARE)
        }

        // Then
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val (center, corner) = logoPositions()
        assertTrue(pixels[center, center].isCloseTo(Color(RED)), "The center of the logo is ${pixels[center, center]}")
        assertTrue(pixels[corner, corner].isCloseTo(Color(RED)), "The corner of the logo is ${pixels[corner, corner]}")
    }

    @Test
    fun `a round logo is cut to its shape`() = runComposeUiTest {
        // When
        setContent {
            logoCode(shape = CodeShape.CIRCLE)
        }

        // Then
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val (center, corner) = logoPositions()
        assertTrue(pixels[center, center].isCloseTo(Color(RED)), "The center of the logo is ${pixels[center, center]}")
        assertTrue(!pixels[corner, corner].isCloseTo(Color(RED)), "The corner of the logo is ${pixels[corner, corner]}")
    }

    @Composable
    private fun logoCode(shape: CodeShape) {
        SCTheme {
            Box(modifier = Modifier.testTag(TAG)) {
                CodeImage(
                    modifier = Modifier.width(240.dp),
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    contentDescription = "QR code",
                    style = CodeStyle(
                        logo = RED_PNG,
                        logoShape = shape,
                    ),
                )
            }
        }
    }

    // The center of the logo, then just inside its corner: a fifth of the code of 208 dp, centered in
    // its image of 240 dp
    private fun ComposeUiTest.logoPositions(): Pair<Int, Int> =
        with(density) {
            val center = 120.dp.toPx()
            center.toInt() to (center - 208.dp.toPx() * 0.2f / 2 + 3).toInt()
        }

    @Test
    fun `a barcode stays dark on white whatever the style`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                Box(modifier = Modifier.testTag(TAG)) {
                    CodeImage(
                        modifier = Modifier.width(300.dp),
                        rawValue = "4006381333931",
                        format = BarcodeFormat.EAN_13,
                        contentDescription = "Barcode",
                        style = CodeStyle(
                            darkColor = INDIGO,
                            lightColor = CREAM,
                        ),
                    )
                }
            }
        }

        // Then
        val pixels = onNodeWithTag(TAG).captureToImage().toPixelMap()
        val margin = with(density) { (16.dp.toPx() / 2).toInt() }
        assertTrue(pixels[margin, margin].isCloseTo(Color.White), "The margin is ${pixels[margin, margin]}")
    }

    private fun Color.distanceTo(other: Color): Float =
        abs(red - other.red) + abs(green - other.green) + abs(blue - other.blue)

    private fun Color.isCloseTo(other: Color): Boolean =
        abs(red - other.red) < TOLERANCE && abs(green - other.green) < TOLERANCE && abs(blue - other.blue) < TOLERANCE

    private companion object {
        const val TAG = "code"
        const val INDIGO = 0xFF3B43BE
        const val CREAM = 0xFFFFF8E1
        const val RED = 0xFFB71C1C
        const val BLACK = 0xFF000000

        const val TOLERANCE = 0.03f
    }
}
