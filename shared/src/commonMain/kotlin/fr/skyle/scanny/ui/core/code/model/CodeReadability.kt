package fr.skyle.scanny.ui.core.code.model

/** How well a scanner tells the dots of a code from its background. */
enum class CodeReadability {
    GOOD,
    // The dots and the background stand out too little from each other
    LOW_CONTRAST,
    // Light dots on a darker background, which many scanners read poorly
    INVERTED,
}
