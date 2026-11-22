package fr.skyle.scanny.domain.mapper

import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle

internal fun CodeStyle.toStoredCodeStyle(): StoredCodeStyle =
    StoredCodeStyle(
        darkColor = darkColor,
        lightColor = lightColor,
        dotShape = dotShape.name,
        cornerShape = cornerShape.name,
        logo = logo,
        errorCorrection = errorCorrection.name,
    )

// A shape this version does not know, written by a later one, is drawn square, and a correction left to
// Scanny
internal fun StoredCodeStyle.toCodeStyle(): CodeStyle =
    CodeStyle(
        darkColor = darkColor,
        lightColor = lightColor,
        dotShape = dotShape.toCodeShape(),
        cornerShape = cornerShape.toCodeShape(),
        logo = logo,
        errorCorrection = errorCorrection.toCodeErrorCorrection(),
    )

private fun String.toCodeShape(): CodeShape =
    CodeShape.entries.firstOrNull { shape -> shape.name == this } ?: CodeShape.SQUARE

private fun String.toCodeErrorCorrection(): CodeErrorCorrection =
    CodeErrorCorrection.entries.firstOrNull { errorCorrection -> errorCorrection.name == this } ?: CodeErrorCorrection.AUTO
