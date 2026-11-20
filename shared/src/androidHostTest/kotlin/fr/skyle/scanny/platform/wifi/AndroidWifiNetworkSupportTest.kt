package fr.skyle.scanny.platform.wifi

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidWifiNetworkSupportTest {

    @Test
    fun `from android 11 a protected network with a valid passphrase can be joined`() {
        // Given
        val wifiNetworkSupport = AndroidWifiNetworkSupport(sdkInt = ANDROID_11)

        // When
        val canJoin = listOf(WifiEncryption.WPA, WifiEncryption.WPA3).map { encryption ->
            wifiNetworkSupport.canJoin(wifi = wifi(encryption = encryption))
        }

        // Then
        assertEquals(
            expected = listOf(true, true),
            actual = canJoin,
        )
    }

    @Test
    fun `before android 11 no network can be joined`() {
        // Given
        val wifiNetworkSupport = AndroidWifiNetworkSupport(sdkInt = ANDROID_10)

        // When
        val canJoin = wifiNetworkSupport.canJoin(wifi = wifi(encryption = WifiEncryption.WPA))

        // Then
        assertEquals(
            expected = false,
            actual = canJoin,
        )
    }

    @Test
    fun `an open network can be joined but a wep one cannot`() {
        // Given
        val wifiNetworkSupport = AndroidWifiNetworkSupport(sdkInt = ANDROID_11)

        // When
        val canJoin = listOf(WifiEncryption.NONE, WifiEncryption.WEP).map { encryption ->
            wifiNetworkSupport.canJoin(
                wifi = wifi(
                    encryption = encryption,
                    password = null,
                ),
            )
        }

        // Then
        assertEquals(
            expected = listOf(true, false),
            actual = canJoin,
        )
    }

    @Test
    fun `a password the system would refuse as a passphrase cannot be used`() {
        // Given
        val wifiNetworkSupport = AndroidWifiNetworkSupport(sdkInt = ANDROID_11)
        val passwords = listOf(
            null,
            "short",
            "a".repeat(64),
            "mot-de-passe-été",
        )

        // When
        val canJoin = passwords.map { password ->
            wifiNetworkSupport.canJoin(
                wifi = wifi(
                    encryption = WifiEncryption.WPA,
                    password = password,
                ),
            )
        }

        // Then
        assertEquals(
            expected = listOf(false, false, false, false),
            actual = canJoin,
        )
    }

    private fun wifi(
        encryption: WifiEncryption,
        password: String? = "secret12",
    ): BarcodeContent.Wifi =
        BarcodeContent.Wifi(
            ssid = "Scanny",
            encryption = encryption,
            password = password,
        )

    private companion object {
        const val ANDROID_10 = 29
        const val ANDROID_11 = 30
    }
}