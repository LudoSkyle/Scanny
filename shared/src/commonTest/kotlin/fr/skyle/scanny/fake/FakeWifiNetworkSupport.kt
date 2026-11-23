package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.platform.wifi.WifiNetworkSupport

internal class FakeWifiNetworkSupport(
    private val canJoin: Boolean = false,
) : WifiNetworkSupport {

    override fun canJoin(wifi: BarcodeContent.Wifi): Boolean =
        canJoin
}