package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.test.ComposeTest
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

    private fun Color.isCloseTo(other: Color): Boolean =
        abs(red - other.red) < TOLERANCE && abs(green - other.green) < TOLERANCE && abs(blue - other.blue) < TOLERANCE

    private companion object {
        const val TAG = "code"
        const val INDIGO = 0xFF3B43BE
        const val CREAM = 0xFFFFF8E1
        const val TOLERANCE = 0.03f
    }
}
