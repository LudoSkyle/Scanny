package fr.skyle.scanny.domain.mapper

import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle

internal fun CodeStyle.toStoredCodeStyle(): StoredCodeStyle =
    StoredCodeStyle(
        darkColor = darkColor,
        lightColor = lightColor,
        cornerColor = cornerColor,
        gradient = gradient.name,
        gradientColor = gradientColor,
        dotShape = dotShape.name,
        cornerShape = cornerShape.name,
        logo = logo,
        logoShape = logoShape.name,
        logoMargin = logoMargin.name,
        errorCorrection = errorCorrection.name,
    )

// A shape this version does not know, written by a later one, is drawn square, a gradient left out, a
// margin normal, and a correction left to Scanny
internal fun StoredCodeStyle.toCodeStyle(): CodeStyle =
    CodeStyle(
        darkColor = darkColor,
        lightColor = lightColor,
        cornerColor = cornerColor,
        gradient = gradient.toCodeGradient(),
        gradientColor = gradientColor,
        dotShape = dotShape.toCodeShape(),
        cornerShape = cornerShape.toCodeShape(),
        logo = logo,
        logoShape = logoShape.toCodeShape(),
        logoMargin = logoMargin.toCodeLogoMargin(),
        errorCorrection = errorCorrection.toCodeErrorCorrection(),
    )

private fun String.toCodeShape(): CodeShape =
    CodeShape.entries.firstOrNull { shape -> shape.name == this } ?: CodeShape.SQUARE

private fun String.toCodeGradient(): CodeGradient =
    CodeGradient.entries.firstOrNull { gradient -> gradient.name == this } ?: CodeGradient.NONE

private fun String.toCodeLogoMargin(): CodeLogoMargin =
    CodeLogoMargin.entries.firstOrNull { margin -> margin.name == this } ?: CodeLogoMargin.NORMAL

private fun String.toCodeErrorCorrection(): CodeErrorCorrection =
    CodeErrorCorrection.entries.firstOrNull { errorCorrection -> errorCorrection.name == this } ?: CodeErrorCorrection.AUTO
