package fr.skyle.scanny.platform.wifi

import androidx.compose.runtime.Composable
import fr.skyle.scanny.domain.model.BarcodeContent

/** What came of joining a Wi-Fi network, for the app to tell it. */
internal enum class WifiJoinResult {
    // Saved by the system: the device joins it when in range
    SAVED,

    // Read from the screen of the system only: the joiner opens the Wi-Fi settings instead, where it is
    ALREADY_SAVED,
    NOT_SAVED,

    // Declined by the user on the screen of the system: nothing to tell
    CANCELED,

    // The Wi-Fi settings opened for the user to choose the network there, told over them by the platform
    SETTINGS_OPENED,

    // No screen of the system to save a network, nor to choose one
    NO_SETTINGS,
}

/** Joins a Wi-Fi network read in a code, as far as the device lets an app. */
internal interface WifiNetworkJoiner {

    /** Saves [wifi] through the screen of the system where there is one, or else through the Wi-Fi settings. */
    fun join(wifi: BarcodeContent.Wifi)

    /** Opens the Wi-Fi settings, the password of [wifi] copied, for the user to choose it there. */
    fun joinFromSettings(wifi: BarcodeContent.Wifi)
}

/**
 * Returns the joiner of the networks [WifiNetworkSupport] says the device can join; [onResult] tells what
 * came of each.
 */
@Composable
internal expect fun rememberWifiNetworkJoiner(
    onResult: (wifi: BarcodeContent.Wifi, result: WifiJoinResult) -> Unit,
): WifiNetworkJoiner
