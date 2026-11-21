package fr.skyle.scanny.ui.core.code.image

import android.graphics.BitmapFactory
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The images of the codes, drawn for real by the native graphics of Robolectric, read back by ZXing.
 * Vision finds no code on the iOS simulator: the JVM checks that every style stays readable.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CodeImageReadabilityTest {

    private val renderer = QroseCodeImageRenderer()

    @Test
    fun `a qr code is read back in every shape`() = runTest {
        // Given
        // In the colors of the palette that stand out the least: red on lavender
        val styles = CodeShape.entries.flatMap { dotShape ->
            CodeShape.entries.map { cornerShape ->
                CodeStyle(
                    darkColor = CodeDotColor.RED.argb,
                    lightColor = CodeBackgroundColor.LAVENDER.argb,
                    dotShape = dotShape,
                    cornerShape = cornerShape,
                )
            }
        }

        // When
        val unreadStyles = styles.filterNot { style -> renderQrCode(style = style).readQrCode() == TEXT }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = unreadStyles,
        )
    }

    @Test
    fun `a qr code is read back in every pair of colors of the palette`() = runTest {
        // Given
        // Round dots and corners: the shapes that cover the least of the code
        val styles = CodeDotColor.entries.flatMap { dotColor ->
            CodeBackgroundColor.entries.map { backgroundColor ->
                CodeStyle(
                    darkColor = dotColor.argb,
                    lightColor = backgroundColor.argb,
                    dotShape = CodeShape.CIRCLE,
                    cornerShape = CodeShape.CIRCLE,
                )
            }
        }

        // When
        val unreadStyles = styles.filterNot { style -> renderQrCode(style = style).readQrCode() == TEXT }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = unreadStyles,
        )
    }

    private suspend fun renderQrCode(style: CodeStyle): ByteArray =
        renderer.renderPng(
            rawValue = TEXT,
            format = BarcodeFormat.QR_CODE,
            style = style,
        )

    private fun ByteArray.readQrCode(): String? {
        val bitmap = BitmapFactory.decodeByteArray(this, 0, size)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val image = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(bitmap.width, bitmap.height, pixels)))
        // ZXing throws when it finds no code
        return runCatching { QRCodeReader().decode(image).text }.getOrNull()
    }

    private companion object {
        // As long as a link a user would put in a code
        const val TEXT = "https://scanny.app/created/code?from=editor&style=custom"
    }
}
