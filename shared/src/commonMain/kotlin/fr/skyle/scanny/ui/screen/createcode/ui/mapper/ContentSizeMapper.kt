package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection

// The modes qrose chooses between, for a whole text: digits only, or upper case letters and digits
private val NUMERIC = Regex("^\\d+$")
private val ALPHANUMERIC = Regex("^[0-9A-Z $%*+\\-./:]+$")

/** The most a QR code holds in each mode, at its largest version (40). */
private data class QrCapacity(
    val numeric: Int,
    val alphanumeric: Int,
    val bytes: Int,
)

private fun QrErrorCorrection.capacity(): QrCapacity =
    when (this) {
        QrErrorCorrection.L -> QrCapacity(numeric = 7089, alphanumeric = 4296, bytes = 2953)
        QrErrorCorrection.M -> QrCapacity(numeric = 5596, alphanumeric = 3391, bytes = 2331)
        QrErrorCorrection.Q -> QrCapacity(numeric = 3993, alphanumeric = 2420, bytes = 1663)
        QrErrorCorrection.H -> QrCapacity(numeric = 3057, alphanumeric = 1852, bytes = 1273)
    }

/**
 * How much of a QR code [rawValue] takes, against the most it holds at [errorCorrection]. qrose writes
 * a whole text in a single mode, counted in its own unit: digits only, upper case letters and digits
 * with ` $%*+-./:`, or else the bytes of its UTF-8, where an accent or an emoji takes several.
 */
internal fun qrContentSize(
    rawValue: String,
    errorCorrection: QrErrorCorrection,
): ContentSize {
    val capacity = errorCorrection.capacity()
    return when {
        rawValue.matches(NUMERIC) -> ContentSize(
            used = rawValue.length,
            max = capacity.numeric,
            hasWideCharacters = false,
        )

        rawValue.matches(ALPHANUMERIC) -> ContentSize(
            used = rawValue.length,
            max = capacity.alphanumeric,
            hasWideCharacters = false,
        )

        else -> {
            val byteCount = rawValue.encodeToByteArray().size
            ContentSize(
                used = byteCount,
                max = capacity.bytes,
                hasWideCharacters = byteCount > rawValue.length,
            )
        }
    }
}
