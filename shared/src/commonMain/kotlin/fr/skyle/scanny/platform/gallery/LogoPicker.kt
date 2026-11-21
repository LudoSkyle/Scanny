package fr.skyle.scanny.platform.gallery

import androidx.compose.runtime.Composable
import kotlin.math.roundToInt

sealed interface LogoPickResult {

    /** The image picked, upright, as a PNG of [LOGO_MAX_SIDE] pixels a side at most. */
    class Picked(val png: ByteArray) : LogoPickResult

    data object Unreadable : LogoPickResult
}

/**
 * Returns the action that lets the user pick an image for the logo of a code, and reports it reduced.
 * Nothing is reported when the user cancels.
 */
@Composable
expect fun rememberLogoPicker(onResult: (LogoPickResult) -> Unit): () -> Unit

// The longest side of a logo, in pixels: more than the logo takes of the image of a code, a fifth of
// a thousand pixels, and light enough to be kept with the creation
internal const val LOGO_MAX_SIDE = 256

/** The size an image is reduced to for a logo, its proportions kept, never enlarged. */
internal fun logoSize(
    width: Int,
    height: Int,
): Pair<Int, Int> {
    val scale = minOf(1f, LOGO_MAX_SIDE.toFloat() / maxOf(width, height))
    return (width * scale).roundToInt().coerceAtLeast(1) to (height * scale).roundToInt().coerceAtLeast(1)
}
