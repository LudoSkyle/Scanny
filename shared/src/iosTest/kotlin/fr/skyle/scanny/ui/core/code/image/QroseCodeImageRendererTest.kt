package fr.skyle.scanny.ui.core.code.image

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.platform.image.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.UIKit.UIImage
import kotlin.test.Test
import kotlin.test.assertEquals

// The drawing of Compose needs its engine, which the iOS simulator runs: the JVM tests do not
@OptIn(ExperimentalForeignApi::class)
class QroseCodeImageRendererTest {

    private val renderer = QroseCodeImageRenderer()

    @Test
    fun `a qr code is drawn into a square png`() = runTest {
        // When
        val png = renderer.renderPng(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(),
        )

        // Then
        assertEquals(
            expected = PNG_SIGNATURE to (1024.0 to 1024.0),
            actual = png.take(PNG_SIGNATURE.size) to png.imageSize(),
        )
    }

    @Test
    fun `a barcode is drawn into a png wider than high`() = runTest {
        // When
        val png = renderer.renderPng(
            rawValue = "4006381333931",
            format = BarcodeFormat.EAN_13,
            style = CodeStyle(),
        )

        // Then
        assertEquals(
            expected = PNG_SIGNATURE to (1200.0 to 480.0),
            actual = png.take(PNG_SIGNATURE.size) to png.imageSize(),
        )
    }

    // An image read from data keeps its scale of 1: its size is its size in pixels
    private fun ByteArray.imageSize(): Pair<Double, Double> =
        checkNotNull(UIImage(data = toNSData())).size.useContents { width to height }

    private companion object {
        // The first bytes of any PNG file
        val PNG_SIGNATURE = listOf<Byte>(-119, 80, 78, 71)
    }
}