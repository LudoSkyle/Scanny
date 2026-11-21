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
