package fr.skyle.scanny.domain.model

/** A code read by the detection engine of the platform: its raw text and its format. */
data class DetectedBarcode(
    val rawValue: String,
    val format: BarcodeFormat,
)

// An EAN-13 starting with 0 is a UPC-A: ML Kit reports it as such, AVFoundation and Vision as an
// EAN-13. Normalized, both platforms give the same result
fun DetectedBarcode.normalized(): DetectedBarcode =
    if (format == BarcodeFormat.EAN_13 && rawValue.length == 13 && rawValue.startsWith(char = '0')) {
        copy(
            rawValue = rawValue.drop(1),
            format = BarcodeFormat.UPC_A,
        )
    } else {
        this
    }