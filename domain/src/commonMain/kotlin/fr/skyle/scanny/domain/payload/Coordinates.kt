package fr.skyle.scanny.domain.payload

import kotlin.math.abs
import kotlin.math.roundToLong

private const val COORDINATE_DECIMALS = 7
private const val COORDINATE_SCALE = 10_000_000L

/**
 * A latitude or a longitude as a code writes it: in decimals, never in exponent form, rounded to seven
 * places, about a centimeter on the ground. The text of a Double differs between the platforms, and is
 * "1.0E-5" for the small ones.
 */
fun Double.toCoordinateText(): String {
    val scaled = (abs(this) * COORDINATE_SCALE).roundToLong()
    val decimals = (scaled % COORDINATE_SCALE).toString()
        .padStart(
            length = COORDINATE_DECIMALS,
            padChar = '0',
        )
        .trimEnd('0')

    return buildString {
        if (this@toCoordinateText < 0 && scaled != 0L) {
            append('-')
        }
        append(scaled / COORDINATE_SCALE)
        if (decimals.isNotEmpty()) {
            append('.')
            append(decimals)
        }
    }
}
