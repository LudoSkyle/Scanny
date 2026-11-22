package fr.skyle.scanny.ui.screen.createcode.ui.model

/** How much of a QR code its content takes, against the most the code can hold. */
data class ContentSize(
    val used: Int,
    val max: Int,
    // An accent or an emoji takes several bytes of the code: the counter says so
    val hasWideCharacters: Boolean,
) {
    val isOverLimit: Boolean
        get() = used > max
}
