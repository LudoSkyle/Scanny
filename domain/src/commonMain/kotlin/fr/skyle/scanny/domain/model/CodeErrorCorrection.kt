package fr.skyle.scanny.domain.model

/**
 * How much of a QR code can be lost, damaged or hidden by a logo, and the code still read. The more,
 * the more room the correction takes from the content: a lower correction holds a longer content, in a
 * less dense code.
 */
enum class CodeErrorCorrection {
    // The lowest, or the highest with a logo, which needs it
    AUTO,

    // About 7 % of the code
    LOW,

    // About 15 %
    MEDIUM,

    // About 25 %
    QUARTILE,

    // About 30 %
    HIGH,
}
