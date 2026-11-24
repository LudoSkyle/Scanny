package fr.skyle.scanny.ui.core.code.image

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.platform.image.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSXMLParser
import platform.UIKit.UIImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `a qr code is drawn into a well formed svg image of the size of its png`() = runTest {
        // When
        val svg = renderer.renderSvg(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(),
        )

        // Then
        assertTrue(svg.isWellFormedXml())
        assertTrue(svg.contains("width=\"1024\" height=\"1024\" viewBox=\"0 0 1024 1024\""))
        assertTrue(svg.contains("<path "))
    }

    @Test
    fun `the gradient of a qr code is an svg gradient its dots are filled with`() = runTest {
        // When
        val svg = renderer.renderSvg(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(
                gradient = CodeGradient.LINEAR,
                dotShape = CodeShape.CIRCLE,
            ),
        )

        // Then
        assertTrue(svg.isWellFormedXml())
        assertTrue(svg.contains("<linearGradient id=\"gradient\""))
        assertTrue(svg.contains("fill=\"url(#gradient)\""))
    }

    @Test
    fun `the logo of a qr code is embedded in its svg image with the blank around it`() = runTest {
        // Given
        val logo = renderer.renderPng(
            rawValue = "4006381333931",
            format = BarcodeFormat.EAN_13,
            style = CodeStyle(),
        )

        // When
        val svg = renderer.renderSvg(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(
                logo = logo,
                logoShape = CodeShape.CIRCLE,
                logoMargin = CodeLogoMargin.NORMAL,
            ),
        )

        // Then
        assertTrue(svg.isWellFormedXml())
        assertTrue(svg.contains("href=\"data:image/png;base64,"))
        assertTrue(svg.contains("<mask "))
    }

    @Test
    fun `a barcode is drawn into an svg image wider than high`() = runTest {
        // When
        val svg = renderer.renderSvg(
            rawValue = "4006381333931",
            format = BarcodeFormat.EAN_13,
            style = CodeStyle(),
        )

        // Then
        assertTrue(svg.isWellFormedXml())
        assertTrue(svg.contains("width=\"1200\" height=\"480\""))
    }

    private fun String.isWellFormedXml(): Boolean =
        NSXMLParser(data = encodeToByteArray().toNSData()).parse()

    // An image read from data keeps its scale of 1: its size is its size in pixels
    private fun ByteArray.imageSize(): Pair<Double, Double> =
        checkNotNull(UIImage(data = toNSData())).size.useContents { width to height }

    private companion object {
        // The first bytes of any PNG file
        val PNG_SIGNATURE = listOf<Byte>(-119, 80, 78, 71)
    }
}