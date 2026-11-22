package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.platform.wifi.WifiJoinResult
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.wifi_already_saved
import fr.skyle.scanny.resources.wifi_not_saved
import fr.skyle.scanny.resources.wifi_saved
import org.jetbrains.compose.resources.getString

/**
 * Tells what came of joining [wifi], and whether the user asked for the Wi-Fi settings from the message:
 * offered when the system did not save the network, already saved or refused. Nothing when the user
 * declined the screen of the system, nor when the Wi-Fi settings opened over the app.
 */
internal suspend fun SCSnackbarState.showWifiJoinResult(
    wifi: BarcodeContent.Wifi,
    result: WifiJoinResult,
): Boolean =
    when (result) {
        WifiJoinResult.SAVED -> show(message = getString(Res.string.wifi_saved, wifi.ssid)).let { false }

        WifiJoinResult.ALREADY_SAVED -> show(
            message = getString(Res.string.wifi_already_saved, wifi.ssid),
            actionLabel = getString(Res.string.wifi),
        )

        WifiJoinResult.NOT_SAVED -> show(
            message = getString(Res.string.wifi_not_saved, wifi.ssid),
            actionLabel = getString(Res.string.wifi),
        )

        WifiJoinResult.NO_SETTINGS -> show(message = getString(Res.string.no_app_to_open)).let { false }

        // Over the settings, a message of the app would not be seen: the platform told the user there
        WifiJoinResult.SETTINGS_OPENED,
        WifiJoinResult.CANCELED,
        -> false
    }
