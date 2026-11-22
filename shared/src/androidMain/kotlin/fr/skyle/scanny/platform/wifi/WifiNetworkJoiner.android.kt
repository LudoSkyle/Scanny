package fr.skyle.scanny.platform.wifi

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.wifi_choose_in_settings
import fr.skyle.scanny.resources.wifi_password_copied
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

// Said sensitive, the password is not shown in the preview of the clipboard, from Android 13
private const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"

@Composable
internal actual fun rememberWifiNetworkJoiner(
    onResult: (wifi: BarcodeContent.Wifi, result: WifiJoinResult) -> Unit,
): WifiNetworkJoiner {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    // The network on the screen of the system, told back with its result
    var pendingWifi by remember { mutableStateOf<BarcodeContent.Wifi?>(null) }
    // The settings only show their screen to an app that waits for its result
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        pendingWifi?.let { wifi ->
            currentOnResult(wifi, wifiJoinResult(resultCode = result.resultCode, data = result.data))
        }
        pendingWifi = null
    }

    return remember(context, coroutineScope, launcher) {
        object : WifiNetworkJoiner {
            override fun join(wifi: BarcodeContent.Wifi) {
                if (canSaveWithSystemScreen(wifi = wifi, sdkInt = Build.VERSION.SDK_INT)) {
                    try {
                        pendingWifi = wifi
                        launcher.launch(addNetworkIntent(wifi = wifi))
                        return
                    } catch (exception: ActivityNotFoundException) {
                        pendingWifi = null
                        Logger.e(throwable = exception) { "No screen of the system adds a network" }
                    }
                }
                joinFromSettings(wifi = wifi)
            }

            override fun joinFromSettings(wifi: BarcodeContent.Wifi) {
                coroutineScope.launch {
                    val instructions = if (wifi.password != null) {
                        getString(Res.string.wifi_password_copied, wifi.ssid)
                    } else {
                        getString(Res.string.wifi_choose_in_settings, wifi.ssid)
                    }
                    currentOnResult(wifi, openWifiSettings(context = context, wifi = wifi, instructions = instructions))
                }
            }
        }
    }
}

/**
 * Whether the screen of the system that saves a network takes [wifi]: from Android 11, an open network
 * or a WPA one with a passphrase the system accepts, never a WEP one, nor an enterprise one, which a
 * suggestion takes only with the certificate of its server. Before, an app can no longer save a network
 * itself: the Wi-Fi settings are left to the user.
 */
internal fun canSaveWithSystemScreen(
    wifi: BarcodeContent.Wifi,
    sdkInt: Int,
): Boolean =
    sdkInt >= Build.VERSION_CODES.R && when (wifi.encryption) {
        WifiEncryption.NONE -> true
        WifiEncryption.WEP,
        WifiEncryption.WPA_ENTERPRISE,
        -> false
        WifiEncryption.WPA -> wifi.password?.isPassphrase() == true
    }

// What the system takes as a WPA passphrase: 8 to 63 printable ASCII characters
private fun String.isPassphrase(): Boolean =
    length in 8..63 && all { char -> char.code in 0x20..0x7E }

/**
 * What came of the screen of the system that saves a network: its result for the network, or else
 * whether the user went through it.
 */
internal fun wifiJoinResult(
    resultCode: Int,
    data: Intent?,
): WifiJoinResult {
    val networkResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        data?.getIntegerArrayListExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST)?.firstOrNull()
    } else {
        null
    }
    return when {
        networkResult == Settings.ADD_WIFI_RESULT_SUCCESS -> WifiJoinResult.SAVED
        networkResult == Settings.ADD_WIFI_RESULT_ALREADY_EXISTS -> WifiJoinResult.ALREADY_SAVED
        networkResult == Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED -> WifiJoinResult.NOT_SAVED
        resultCode == Activity.RESULT_OK -> WifiJoinResult.SAVED
        else -> WifiJoinResult.CANCELED
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun addNetworkIntent(wifi: BarcodeContent.Wifi): Intent {
    val suggestion = WifiNetworkSuggestion.Builder()
        .setSsid(wifi.ssid)
        .setIsHiddenSsid(wifi.isHidden)
        .apply {
            val password = wifi.password
            when (wifi.encryption) {
                // SAE only for a WPA3 only network, the passphrase of WPA2 else
                WifiEncryption.WPA -> if (password != null) {
                    if (WifiTransitionMode.WPA3_PERSONAL in wifi.transitionDisable) setWpa3Passphrase(password) else setWpa2Passphrase(password)
                }
                WifiEncryption.NONE -> setIsEnhancedOpen(WifiTransitionMode.ENHANCED_OPEN in wifi.transitionDisable)
                // A WEP or an enterprise network never comes here
                WifiEncryption.WEP,
                WifiEncryption.WPA_ENTERPRISE,
                -> Unit
            }
        }
        .build()

    return Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
        .putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(suggestion))
}

/**
 * Copies the password of [wifi] and opens the Wi-Fi settings, for the user to choose it there: the panel
 * over the app from Android 10, the screen of the settings before. [instructions] show in a toast, the
 * one message of an app the system draws over another.
 */
internal fun openWifiSettings(
    context: Context,
    wifi: BarcodeContent.Wifi,
    instructions: String,
): WifiJoinResult {
    val password = wifi.password
    if (password != null) {
        val clip = ClipData.newPlainText(wifi.ssid, password).apply {
            description.extras = PersistableBundle().apply { putBoolean(EXTRA_IS_SENSITIVE, true) }
        }
        context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
    }

    val intent = Intent(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Settings.Panel.ACTION_WIFI else Settings.ACTION_WIFI_SETTINGS)
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        Toast.makeText(context, instructions, Toast.LENGTH_LONG).show()
        WifiJoinResult.SETTINGS_OPENED
    } catch (exception: ActivityNotFoundException) {
        Logger.e(throwable = exception) { "No Wi-Fi settings to open" }
        WifiJoinResult.NO_SETTINGS
    }
}
