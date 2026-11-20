package fr.skyle.scanny.platform.wifi

import android.os.Build
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption

internal class AndroidWifiNetworkSupport(
    private val sdkInt: Int,
) : WifiNetworkSupport {

    // The screen that adds a network appeared with Android 11, and does not take WEP
    override fun canJoin(wifi: BarcodeContent.Wifi): Boolean =
        sdkInt >= Build.VERSION_CODES.R && when (wifi.encryption) {
            WifiEncryption.NONE -> true
            WifiEncryption.WEP -> false
            WifiEncryption.WPA,
            WifiEncryption.WPA3,
            -> wifi.password?.isPassphrase() == true
        }
}

// What the system takes as a WPA passphrase: 8 to 63 printable ASCII characters
private fun String.isPassphrase(): Boolean =
    length in 8..63 && all { char -> char.code in 0x20..0x7E }