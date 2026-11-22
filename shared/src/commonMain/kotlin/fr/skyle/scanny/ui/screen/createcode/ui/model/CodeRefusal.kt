package fr.skyle.scanny.ui.screen.createcode.ui.model

/** Why the format chosen cannot hold the content of the form. */
enum class CodeRefusal {
    // More than the largest code of the format holds
    TOO_LONG,

    // A character beyond ISO 8859-1, which a Data Matrix and an Aztec code do not hold
    CHARACTERS_NOT_HELD,

    // A barcode's wrong length, character or check digit
    VALUE_NOT_FOR_FORMAT,
}
