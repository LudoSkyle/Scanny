package fr.skyle.scanny.platform.camera

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode
import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi

/**
 * Debug tool, see [SimulatedScanCamera]: in a debug build on the simulator, which has no camera, the code
 * given in the environment of the app is read as if the camera saw it, once per launch. The UI tests of
 * `iosApp` set it; by hand: `SIMCTL_CHILD_SCANNY_SIMULATED_CODE=… xcrun simctl launch booted fr.skyle.scanny`.
 */
internal object SimulatedScan {

    private const val CODE_VARIABLE = "SCANNY_SIMULATED_CODE"
    private const val FORMAT_VARIABLE = "SCANNY_SIMULATED_FORMAT"

    private val environment = NSProcessInfo.processInfo.environment

    @OptIn(ExperimentalNativeApi::class)
    val isEnabled: Boolean =
        Platform.isDebugBinary && environment["SIMULATOR_UDID"] != null && environment[CODE_VARIABLE] != null

    private var isRead = false

    fun take(): DetectedBarcode? {
        val rawValue = environment[CODE_VARIABLE] as? String
        if (!isEnabled || isRead || rawValue == null) {
            return null
        }

        isRead = true
        return DetectedBarcode(
            rawValue = rawValue,
            format = BarcodeFormat.entries.firstOrNull { it.name == environment[FORMAT_VARIABLE] } ?: BarcodeFormat.QR_CODE,
        )
    }
}