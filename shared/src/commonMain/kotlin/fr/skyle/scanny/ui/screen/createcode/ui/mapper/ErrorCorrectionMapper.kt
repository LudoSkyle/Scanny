package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.mapper.allowsLogo
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.core.code.mapper.qrErrorCorrection
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection

/**
 * The corrections offered for a QR code of [rawValue] in [style]: one too high for the content, or too
 * low for its logo, is told so. A content still incomplete fits any.
 */
internal fun errorCorrectionChoices(
    rawValue: String?,
    style: CodeStyle,
): List<ErrorCorrectionChoice> =
    CodeErrorCorrection.entries.map { errorCorrection ->
        val styleWithIt = style.copy(errorCorrection = errorCorrection)
        ErrorCorrectionChoice(
            errorCorrection = errorCorrection,
            blocker = when {
                style.logo != null && !errorCorrection.allowsLogo -> ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO
                rawValue != null && !rawValue.fitsAt(styleWithIt.qrErrorCorrection()) -> ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT
                else -> null
            },
        )
    }

/**
 * What keeps a logo from going on a QR code of [rawValue] in [style], null when nothing does, or when
 * it already has one: it is then changed or removed.
 */
internal fun logoBlocker(
    rawValue: String?,
    style: CodeStyle,
): LogoBlocker? =
    when {
        style.logo != null -> null
        !style.errorCorrection.allowsLogo -> LogoBlocker.CORRECTION_TOO_LOW
        rawValue != null && !rawValue.fitsAt(QrErrorCorrection.H) -> LogoBlocker.CONTENT_TOO_LONG
        else -> null
    }

private fun String.fitsAt(errorCorrection: QrErrorCorrection): Boolean =
    BarcodeFormat.QR_CODE.canHold(
        rawValue = this,
        errorCorrection = errorCorrection,
    )
