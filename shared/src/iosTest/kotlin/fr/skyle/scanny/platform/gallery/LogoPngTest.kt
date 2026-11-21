package fr.skyle.scanny.platform.gallery

import fr.skyle.scanny.platform.image.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIRectFill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalForeignApi::class)
class LogoPngTest {

    @Test
    fun `a photo is reduced to the size of a logo as a png`() {
        // Given
        val photo = pngOf(
            width = 1000.0,
            height = 500.0,
        )

        // When
        val logo = checkNotNull(photo.toLogoPng())

        // Then
        assertEquals(
            expected = PNG_SIGNATURE to (256.0 to 128.0),
            actual = logo.take(PNG_SIGNATURE.size) to logo.imageSize(),
        )
    }

    @Test
    fun `data that holds no image gives no logo`() {
        assertNull(byteArrayOf(1, 2, 3).toNSData().toLogoPng())
    }

    private fun pngOf(
        width: Double,
        height: Double,
    ): NSData {
        val format = UIGraphicsImageRendererFormat.defaultFormat().apply { scale = 1.0 }
        return UIGraphicsImageRenderer(
            size = CGSizeMake(width = width, height = height),
            format = format,
        ).PNGDataWithActions { _ ->
            UIColor.orangeColor.setFill()
            UIRectFill(CGRectMake(x = 0.0, y = 0.0, width = width, height = height))
        }
    }

    // An image read from data keeps its scale of 1: its size is its size in pixels
    private fun ByteArray.imageSize(): Pair<Double, Double> =
        checkNotNull(UIImage(data = toNSData())).size.useContents { width to height }

    private companion object {
        // The first bytes of any PNG file
        val PNG_SIGNATURE = listOf<Byte>(-119, 80, 78, 71)
    }
}
