package fr.skyle.scanny.ui.screen.createcode.ui.model

/** A color offered for the three corners of a QR code: the one of its dots, or one of the palette. */
sealed interface CornerColorOption {

    data object LikeDots : CornerColorOption

    data class Palette(val color: CodeDotColor) : CornerColorOption
}
