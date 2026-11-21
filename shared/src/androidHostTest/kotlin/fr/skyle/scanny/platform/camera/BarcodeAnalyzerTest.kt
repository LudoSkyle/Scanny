package fr.skyle.scanny.platform.camera

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BarcodeAnalyzerTest {

    @Test
    fun `the center is half of the shorter side in the middle of the image`() {
        // When
        val crop = centerCrop(
            width = 1920,
            height = 1440,
        )

        // Then
        assertEquals(
            expected = CenterCrop(
                left = 600,
                top = 360,
                side = 720,
            ),
            actual = crop,
        )
    }

    @Test
    fun `the side and the corner of the center are even`() {
        // When
        val crop = centerCrop(
            width = 1001,
            height = 751,
        )

        // Then
        assertEquals(
            expected = CenterCrop(
                left = 312,
                top = 188,
                side = 374,
            ),
            actual = crop,
        )
    }

    @Test
    fun `the luminance of the center is copied row by row without the padding of the rows`() {
        // Given
        // A 6x6 image, each row padded to 8 bytes: the byte of a pixel is 16 times its row plus its column
        val luma = ByteBuffer.wrap(
            ByteArray(8 * 6) { index ->
                (index / 8 * 16 + index % 8).toByte()
            },
        )
        val target = ByteArray(4)

        // When
        copyCenterLuma(
            luma = luma,
            rowStride = 8,
            crop = centerCrop(
                width = 6,
                height = 6,
            ),
            target = target,
        )

        // Then
        assertContentEquals(
            expected = byteArrayOf(34, 35, 50, 51),
            actual = target,
        )
    }
}