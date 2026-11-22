package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.CodeErrorCorrection

/** A correction offered for a QR code, and what keeps it from being chosen, null when nothing does. */
data class ErrorCorrectionChoice(
    val errorCorrection: CodeErrorCorrection,
    val blocker: ErrorCorrectionBlocker?,
)

enum class ErrorCorrectionBlocker {
    // The content is longer than the code holds at this correction
    TOO_HIGH_FOR_CONTENT,

    // A logo needs the highest correction
    TOO_LOW_FOR_LOGO,
}
