package fr.skyle.scanny.platform.wifi

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class WifiNetworkJoinerTest {

    @Test
    fun `any network can be joined on android`() {
        // When
        val canJoin = WifiEncryption.entries.map { encryption ->
            AndroidWifiNetworkSupport().canJoin(wifi = wifi(encryption = encryption, password = null))
        }

        // Then
        assertEquals(
            expected = List(WifiEncryption.entries.size) { true },
            actual = canJoin,
        )
    }

    @Test
    fun `from android 11 the screen of the system saves an open network or a wpa one with a valid passphrase`() {
        // When
        val canSave = listOf(
            wifi(encryption = WifiEncryption.NONE, password = null),
            wifi(encryption = WifiEncryption.WPA, password = "secret123"),
            wifi(encryption = WifiEncryption.WPA, password = "secret123").copy(transitionDisable = setOf(WifiTransitionMode.WPA3_PERSONAL)),
        ).map { wifi -> canSaveWithSystemScreen(wifi = wifi, sdkInt = ANDROID_11) }

        // Then
        assertEquals(
            expected = listOf(true, true, true),
            actual = canSave,
        )
    }

    @Test
    fun `the settings are left to the user before android 11 for a wep or an enterprise network or a passphrase refused`() {
        // When
        val canSave = listOf(
            canSaveWithSystemScreen(wifi = wifi(encryption = WifiEncryption.WPA, password = "secret123"), sdkInt = ANDROID_10),
            canSaveWithSystemScreen(wifi = wifi(encryption = WifiEncryption.WEP, password = "secret123"), sdkInt = ANDROID_11),
            canSaveWithSystemScreen(wifi = wifi(encryption = WifiEncryption.WPA, password = "short"), sdkInt = ANDROID_11),
            canSaveWithSystemScreen(wifi = wifi(encryption = WifiEncryption.WPA, password = "é".repeat(10)), sdkInt = ANDROID_11),
            canSaveWithSystemScreen(wifi = wifi(encryption = WifiEncryption.WPA_ENTERPRISE, password = "secret123"), sdkInt = ANDROID_11),
        )

        // Then
        assertEquals(
            expected = listOf(false, false, false, false, false),
            actual = canSave,
        )
    }

    @Test
    fun `the result of the screen of the system tells whether the network was saved`() {
        // When
        val results = listOf(
            Settings.ADD_WIFI_RESULT_SUCCESS,
            Settings.ADD_WIFI_RESULT_ALREADY_EXISTS,
            Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED,
        ).map { networkResult ->
            wifiJoinResult(
                resultCode = Activity.RESULT_OK,
                // As the settings put it: a list of integers, not an array
                data = Intent().putIntegerArrayListExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST, arrayListOf(networkResult)),
            )
        }

        // Then
        assertEquals(
            expected = listOf(WifiJoinResult.SAVED, WifiJoinResult.ALREADY_SAVED, WifiJoinResult.NOT_SAVED),
            actual = results,
        )
    }

    @Test
    fun `a screen of the system left without a result is declined`() {
        // When
        val result = wifiJoinResult(
            resultCode = Activity.RESULT_CANCELED,
            data = null,
        )

        // Then
        assertEquals(
            expected = WifiJoinResult.CANCELED,
            actual = result,
        )
    }

    @Test
    @Config(sdk = [28])
    fun `before android 10 the password is copied and the screen of the wi-fi settings opens`() {
        // Given
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        // When
        val result = openWifiSettings(
            context = activity,
            wifi = wifi(encryption = WifiEncryption.WPA, password = "secret123"),
            instructions = "Password copied",
        )

        // Then
        val clip = activity.getSystemService(ClipboardManager::class.java).primaryClip
        assertEquals(
            expected = listOf(WifiJoinResult.SETTINGS_OPENED, Settings.ACTION_WIFI_SETTINGS, "secret123", true, "Password copied"),
            actual = listOf(
                result,
                shadowOf(activity).nextStartedActivity.action,
                clip?.getItemAt(0)?.text?.toString(),
                clip?.description?.extras?.getBoolean("android.content.extra.IS_SENSITIVE"),
                ShadowToast.getTextOfLatestToast(),
            ),
        )
    }

    @Test
    @Config(sdk = [29])
    fun `from android 10 the wi-fi panel opens over the app`() {
        // Given
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        // When
        openWifiSettings(
            context = activity,
            wifi = wifi(encryption = WifiEncryption.WEP, password = "secret123"),
            instructions = "Password copied",
        )

        // Then
        assertEquals(
            expected = Settings.Panel.ACTION_WIFI,
            actual = shadowOf(activity).nextStartedActivity.action,
        )
    }

    @Test
    fun `an open network has no password to copy`() {
        // Given
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        // When
        val result = openWifiSettings(
            context = activity,
            wifi = wifi(encryption = WifiEncryption.NONE, password = null),
            instructions = "Choose the network",
        )

        // Then
        assertEquals(
            expected = WifiJoinResult.SETTINGS_OPENED,
            actual = result,
        )
        assertNull(activity.getSystemService(ClipboardManager::class.java).primaryClip)
    }

    private fun wifi(
        encryption: WifiEncryption,
        password: String?,
    ): BarcodeContent.Wifi =
        BarcodeContent.Wifi(
            ssid = "ScannyNet",
            encryption = encryption,
            password = password,
        )

    private companion object {
        const val ANDROID_10 = 29
        const val ANDROID_11 = 30
    }
}
