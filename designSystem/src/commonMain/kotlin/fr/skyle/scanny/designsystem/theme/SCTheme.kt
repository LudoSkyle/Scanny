package fr.skyle.scanny.designsystem.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

@Composable
fun SCTheme(
    content: @Composable () -> Unit,
) {
    val colors = scannyColors
    val typography = scannyTypography()

    val selectionColors = remember(colors) {
        TextSelectionColors(
            handleColor = colors.primary,
            backgroundColor = colors.primary.copy(alpha = 0.4f),
        )
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalTextSelectionColors provides selectionColors,
        content = content,
    )
}

object SCTheme {
    val colors: SCColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: SCTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}