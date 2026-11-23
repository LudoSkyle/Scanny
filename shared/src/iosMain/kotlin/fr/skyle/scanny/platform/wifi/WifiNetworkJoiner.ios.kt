package fr.skyle.scanny.platform.wifi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.skyle.scanny.domain.model.BarcodeContent

// Never called: IosWifiNetworkSupport offers no network to join
@Composable
internal actual fun rememberWifiNetworkJoiner(
    onResult: (wifi: BarcodeContent.Wifi, result: WifiJoinResult) -> Unit,
): WifiNetworkJoiner =
    remember {
        object : WifiNetworkJoiner {
            override fun join(wifi: BarcodeContent.Wifi) = Unit

            override fun joinFromSettings(wifi: BarcodeContent.Wifi) = Unit
        }
    }
