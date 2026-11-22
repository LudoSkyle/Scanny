package fr.skyle.scanny.domain.model

/**
 * A mode a network refuses to fall back from (Transition Disable of the WPA3 specification), and its bit
 * in the bitmap of the "R:" field.
 */
enum class WifiTransitionMode(val bit: Int) {
    // SAE only, never WPA2: a WPA3-Personal network only
    WPA3_PERSONAL(bit = 0),
    SAE_PK(bit = 1),
    WPA3_ENTERPRISE(bit = 2),
    ENHANCED_OPEN(bit = 3),
}