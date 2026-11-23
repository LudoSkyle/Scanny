package fr.skyle.scanny.ui.core.code.image

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.platform.image.ImageSaver
import fr.skyle.scanny.platform.image.SavedImage
import fr.skyle.scanny.platform.share.ImageSharer

/** Draws the image of a code, then lends it to another app or keeps it in the photos. */
class CodeImageExporter(
    private val codeImageRenderer: CodeImageRenderer,
    private val imageSharer: ImageSharer,
    private val imageSaver: ImageSaver,
) {

    /** Whether the photos can be given an image without a permission: not before Android 10. */
    val canSave: Boolean
        get() = imageSaver.canSave

    /** Throws when the image cannot be drawn or shared. */
    suspend fun share(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
        fileName: String,
    ) {
        imageSharer.shareImage(
            png = codeImageRenderer.renderPng(
                rawValue = rawValue,
                format = format,
                style = style,
            ),
            fileName = fileName,
        )
    }

    /** Returns where the image is kept; throws when it cannot be drawn or saved, the photos refused for instance. */
    suspend fun save(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
        fileName: String,
    ): SavedImage =
        imageSaver.saveImage(
            png = codeImageRenderer.renderPng(
                rawValue = rawValue,
                format = format,
                style = style,
            ),
            fileName = fileName,
        )
}
