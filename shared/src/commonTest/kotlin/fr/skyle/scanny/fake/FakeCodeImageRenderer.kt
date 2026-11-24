package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.image.CodeImageRenderer

/** Gives [png] or [svg] for any code it draws, or fails if [error] is set. */
internal class FakeCodeImageRenderer(
    private val png: ByteArray = byteArrayOf(1, 2, 3),
    private val svg: String = "<svg/>",
    private val error: Exception? = null,
) : CodeImageRenderer {

    val renderedCodes = mutableListOf<RenderedCode>()

    override suspend fun renderPng(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): ByteArray {
        error?.let { throw it }

        renderedCodes += RenderedCode(
            rawValue = rawValue,
            format = format,
            style = style,
        )
        return png
    }

    override suspend fun renderSvg(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): String {
        error?.let { throw it }

        renderedCodes += RenderedCode(
            rawValue = rawValue,
            format = format,
            style = style,
        )
        return svg
    }

    data class RenderedCode(
        val rawValue: String,
        val format: BarcodeFormat,
        val style: CodeStyle,
    )
}