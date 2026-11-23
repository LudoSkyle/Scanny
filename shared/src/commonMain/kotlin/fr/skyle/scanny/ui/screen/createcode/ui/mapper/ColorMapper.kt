package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** A color the picker shows: its hue from 0 to 360, its saturation and its brightness from 0 to 1. */
internal data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

internal fun Long.toHsvColor(): HsvColor {
    val red = ((this shr 16) and 0xFF) / 255f
    val green = ((this shr 8) and 0xFF) / 255f
    val blue = (this and 0xFF) / 255f
    val max = maxOf(red, green, blue)
    val delta = max - minOf(red, green, blue)

    // A grey has no hue: red, the start of the bar
    val hue = when {
        delta == 0f -> 0f
        max == red -> 60f * (((green - blue) / delta).mod(6f))
        max == green -> 60f * ((blue - red) / delta + 2f)
        else -> 60f * ((red - green) / delta + 4f)
    }
    return HsvColor(
        hue = hue,
        saturation = if (max == 0f) 0f else delta / max,
        value = max,
    )
}

/** The color as ARGB, opaque. */
internal fun HsvColor.toArgb(): Long =
    Color.hsv(
        hue = hue.coerceIn(0f, 360f),
        saturation = saturation.coerceIn(0f, 1f),
        value = value.coerceIn(0f, 1f),
    ).toArgb().toLong() and 0xFFFFFFFF

private const val HEX_DIGITS = "0123456789ABCDEFabcdef"
private const val MAX_COMPONENT = 255

/** The opaque ARGB of six hexadecimal digits, with or without a # before them, null otherwise. */
internal fun String.hexToArgb(): Long? {
    val digits = trim().removePrefix("#")
    if (digits.length != 6 || digits.any { digit -> digit !in HEX_DIGITS }) {
        return null
    }
    return 0xFF000000 or digits.toLong(radix = 16)
}

/** The six hexadecimal digits of the color, in capitals, without its alpha. */
internal fun Long.toHex(): String =
    (this and 0xFFFFFF).toString(radix = 16).uppercase().padStart(length = 6, padChar = '0')

/** The opaque ARGB of three components from 0 to 255, null when one is out of them. */
internal fun rgbToArgb(
    red: Int,
    green: Int,
    blue: Int,
): Long? {
    if (listOf(red, green, blue).any { component -> component !in 0..MAX_COMPONENT }) {
        return null
    }
    return 0xFF000000 or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong()
}

internal val Long.red: Int
    get() = ((this shr 16) and 0xFF).toInt()

internal val Long.green: Int
    get() = ((this shr 8) and 0xFF).toInt()

internal val Long.blue: Int
    get() = (this and 0xFF).toInt()
