package fr.skyle.scanny.ui.core.code.model

/** The format the image of a code is shared or saved in: a PNG, or an SVG image, sharp at any size. */
enum class CodeImageFormat(val extension: String) {
    PNG("png"),
    SVG("svg"),
}
