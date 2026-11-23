package fr.skyle.scanny.domain.model

/**
 * How a Wi-Fi network is secured: open (or Wi-Fi Enhanced Open), WEP, a password of WPA2 or WPA3
 * Personal, or an enterprise network of IEEE 802.1X (EAP).
 */
enum class WifiEncryption {
    NONE,
    WEP,
    WPA,
    WPA_ENTERPRISE,
}