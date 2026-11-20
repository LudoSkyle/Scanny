package fr.skyle.scanny.platform.wifi

import androidx.compose.runtime.Composable
import fr.skyle.scanny.domain.model.BarcodeContent

// Never called: IosWifiNetworkSupport offers no network to join
@Composable
internal actual fun rememberWifiNetworkJoiner(): (BarcodeContent.Wifi) -> Unit =
    {}