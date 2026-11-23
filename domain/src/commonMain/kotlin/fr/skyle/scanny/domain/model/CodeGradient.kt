package fr.skyle.scanny.domain.model

/** How the dots of a QR code go from their color to the second color of the gradient, if they do. */
enum class CodeGradient {
    NONE,

    // From the top left corner to the bottom right one
    LINEAR,

    // From the center to the edges
    RADIAL,
}
