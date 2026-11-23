package fr.skyle.scanny.platform.wifi

import fr.skyle.scanny.domain.model.BarcodeContent

// Joining a network needs the Hotspot Configuration entitlement, which a free Apple account does not give
internal class IosWifiNetworkSupport : WifiNetworkSupport {

    override fun canJoin(wifi: BarcodeContent.Wifi): Boolean =
        false
}