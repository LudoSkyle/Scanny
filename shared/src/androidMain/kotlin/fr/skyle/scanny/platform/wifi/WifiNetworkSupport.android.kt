package fr.skyle.scanny.platform.wifi

import fr.skyle.scanny.domain.model.BarcodeContent

// Any network: saved by the screen of the system where it takes it, chosen in the Wi-Fi settings else
internal class AndroidWifiNetworkSupport : WifiNetworkSupport {

    override fun canJoin(wifi: BarcodeContent.Wifi): Boolean =
        true
}
