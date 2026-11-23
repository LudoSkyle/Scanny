package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.image.CodeImageRenderer

/** Gives [png] for any code it draws, or fails if [error] is set. */
internal class FakeCodeImageRenderer(
    private val png: ByteArray = byteArrayOf(1, 2, 3),
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

    data class RenderedCode(
        val rawValue: String,
        val format: BarcodeFormat,
        val style: CodeStyle,
    )
}