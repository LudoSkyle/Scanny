package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat

/** What the editor shows of the code the form describes. */
sealed interface CodePreview {

    /** A field the content needs is still missing or wrong. */
    data object Incomplete : CodePreview

    /** The content is complete, but its format cannot hold it: too long a text, a wrong check digit… */
    data class Refused(
        val format: BarcodeFormat,
        val reason: CodeRefusal,
    ) : CodePreview

    data class Code(
        val rawValue: String,
        val format: BarcodeFormat,
    ) : CodePreview
}