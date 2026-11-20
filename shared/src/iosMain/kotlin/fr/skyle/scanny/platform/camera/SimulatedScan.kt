package fr.skyle.scanny.platform.camera

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode
import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Debug tool, see [SimulatedScanCamera]: in a debug build on the simulator, which has no camera, the
 * codes given in the environment of the app are read as if the camera saw them, once per launch:
 * - `SCANNY_SIMULATED_CODE`, one code, in the format `SCANNY_SIMULATED_FORMAT` (`QR_CODE` by default);
 * - or `SCANNY_SIMULATED_CODES`, several QR codes separated by the record separator (U+001E), read one
 *   after the other, for a multiple scan;
 * - after `SCANNY_SIMULATED_DELAY_MS` (one second by default), the time to start a multiple scan.
 *
 * The UI tests of `iosApp` set them; by hand: `SIMCTL_CHILD_SCANNY_SIMULATED_CODE=… xcrun simctl launch
 * booted fr.skyle.scanny`.
 */
internal object SimulatedScan {

    private const val CODE_VARIABLE = "SCANNY_SIMULATED_CODE"
    private const val CODES_VARIABLE = "SCANNY_SIMULATED_CODES"
    private const val FORMAT_VARIABLE = "SCANNY_SIMULATED_FORMAT"
    private const val DELAY_VARIABLE = "SCANNY_SIMULATED_DELAY_MS"
    private const val CODES_SEPARATOR = '\u001E'

    private val environment = NSProcessInfo.processInfo.environment

    private val barcodes: List<DetectedBarcode> =
        (environment[CODES_VARIABLE] as? String)?.split(CODES_SEPARATOR)?.map { rawValue ->
            DetectedBarcode(
                rawValue = rawValue,
                format = BarcodeFormat.QR_CODE,
            )
        } ?: listOfNotNull(
            (environment[CODE_VARIABLE] as? String)?.let { rawValue ->
                DetectedBarcode(
                    rawValue = rawValue,
                    format = BarcodeFormat.entries.firstOrNull { it.name == environment[FORMAT_VARIABLE] } ?: BarcodeFormat.QR_CODE,
                )
            },
        )

    @OptIn(ExperimentalNativeApi::class)
    val isEnabled: Boolean =
        Platform.isDebugBinary && environment["SIMULATOR_UDID"] != null && barcodes.isNotEmpty()

    val firstReadingDelay: Duration =
        (environment[DELAY_VARIABLE] as? String)?.toLongOrNull()?.milliseconds ?: 1.seconds

    private var isRead = false

    /** The codes to read, the first time only. */
    fun take(): List<DetectedBarcode> {
        if (!isEnabled || isRead) {
            return emptyList()
        }

        isRead = true
        return barcodes
    }
}