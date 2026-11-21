package fr.skyle.scanny.platform.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

// The images decoded and drawn for real, by the native graphics of Robolectric
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LogoPngTest {

    @Test
    fun `a photo is reduced to the size of a logo as a png`() {
        // Given
        val photo = pngOf(
            width = 1000,
            height = 500,
        )

        // When
        val logo = photo.toLogoPng()

        // Then
        assertEquals(
            expected = PNG_SIGNATURE to (256 to 128),
            actual = logo.take(PNG_SIGNATURE.size) to logo.imageSize(),
        )
    }

    // Before Android 9, read without ImageDecoder
    @Test
    @Config(sdk = [27])
    fun `a photo is reduced to the size of a logo before android 9`() {
        // Given
        val photo = pngOf(
            width = 1000,
            height = 500,
        )

        // When
        val logo = photo.toLogoPng()

        // Then
        assertEquals(
            expected = 256 to 128,
            actual = logo.imageSize(),
        )
    }

    @Test
    fun `bytes that hold no image are refused`() {
        assertFails {
            byteArrayOf(1, 2, 3).toLogoPng()
        }
    }

    private fun pngOf(
        width: Int,
        height: Int,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(255, 128, 0))
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }

    private fun ByteArray.imageSize(): Pair<Int, Int> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(this, 0, size, bounds)
        return bounds.outWidth to bounds.outHeight
    }

    private companion object {
        // The first bytes of any PNG file
        val PNG_SIGNATURE = listOf<Byte>(-119, 80, 78, 71)
    }
}
