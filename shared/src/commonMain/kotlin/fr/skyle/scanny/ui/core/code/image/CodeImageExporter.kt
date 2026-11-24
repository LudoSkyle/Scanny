package fr.skyle.scanny.ui.core.code.image

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.platform.image.ImageSaver
import fr.skyle.scanny.platform.image.SavedImage
import fr.skyle.scanny.platform.share.ImageSharer
import fr.skyle.scanny.ui.core.code.model.CodeImageFormat

/** Draws the image of a code, as a PNG or an SVG image, then lends it to another app or keeps it on the device. */
class CodeImageExporter(
    private val codeImageRenderer: CodeImageRenderer,
    private val imageSharer: ImageSharer,
    private val imageSaver: ImageSaver,
) {

    /** Whether the device can be given an image without a permission: not before Android 10. */
    val canSave: Boolean
        get() = imageSaver.canSave

    /** Throws when the image cannot be drawn or shared. */
    suspend fun share(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
        imageFormat: CodeImageFormat,
        fileName: String,
    ) {
        when (imageFormat) {
            CodeImageFormat.PNG -> imageSharer.shareImage(
                png = codeImageRenderer.renderPng(
                    rawValue = rawValue,
                    format = format,
                    style = style,
                ),
                fileName = fileName,
            )

            CodeImageFormat.SVG -> imageSharer.shareSvg(
                svg = codeImageRenderer.renderSvg(
                    rawValue = rawValue,
                    format = format,
                    style = style,
                ),
                fileName = fileName,
            )
        }
    }

    /**
     * Returns where the image is kept, a PNG in the photos, an SVG image in the files, or null when the
     * user chose not to keep it; throws when it cannot be drawn or saved, the photos refused for instance.
     */
    suspend fun save(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
        imageFormat: CodeImageFormat,
        fileName: String,
    ): SavedImage? =
        when (imageFormat) {
            CodeImageFormat.PNG -> imageSaver.saveImage(
                png = codeImageRenderer.renderPng(
                    rawValue = rawValue,
                    format = format,
                    style = style,
                ),
                fileName = fileName,
            )

            CodeImageFormat.SVG -> imageSaver.saveSvg(
                svg = codeImageRenderer.renderSvg(
                    rawValue = rawValue,
                    format = format,
                    style = style,
                ),
                fileName = fileName,
            )
        }
}
