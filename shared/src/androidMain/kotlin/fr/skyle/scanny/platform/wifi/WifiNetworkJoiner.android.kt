package fr.skyle.scanny.platform.wifi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption

@Composable
internal actual fun rememberWifiNetworkJoiner(): (BarcodeContent.Wifi) -> Unit {
    // The settings only show their screen to an app that waits for its result
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // The screen of the system tells the user whether the network was saved
    }

    return remember(launcher) {
        { wifi ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    launcher.launch(addNetworkIntent(wifi = wifi))
                } catch (exception: ActivityNotFoundException) {
                    Logger.e(throwable = exception) { "No screen of the system adds a network" }
                }
            }
        }
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
                WifiEncryption.WPA -> if (password != null) setWpa2Passphrase(password)
                WifiEncryption.WPA3 -> if (password != null) setWpa3Passphrase(password)
                // An open network has no passphrase, a WEP one is never offered
                WifiEncryption.NONE,
                WifiEncryption.WEP,
                -> Unit
            }
        }
        .build()

    return Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
        .putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(suggestion))
}