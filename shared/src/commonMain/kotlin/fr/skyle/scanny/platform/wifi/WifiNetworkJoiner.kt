package fr.skyle.scanny.platform.wifi

import androidx.compose.runtime.Composable
import fr.skyle.scanny.domain.model.BarcodeContent

/**
 * Returns the action that opens the screen of the system adding a network, for the user to save it.
 * Only for a network [WifiNetworkSupport] says the device can join.
 */
@Composable
internal expect fun rememberWifiNetworkJoiner(): (BarcodeContent.Wifi) -> Unit