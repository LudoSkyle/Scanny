package fr.skyle.scanny.platform.wifi

import fr.skyle.scanny.domain.model.BarcodeContent

/** Whether the device can join a network read in a code, from the app. */
interface WifiNetworkSupport {

    fun canJoin(wifi: BarcodeContent.Wifi): Boolean
}