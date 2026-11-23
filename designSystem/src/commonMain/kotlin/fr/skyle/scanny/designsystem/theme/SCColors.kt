package fr.skyle.scanny.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SCColors(
    val transparent: Color,
    val primary: Color,
    val error: Color,
    val success: Color,
    val textPrimary: Color,
    val text: Color,
    val textDark: Color,
    val textLight: Color,
    val textDisabled: Color,
    val textBlack: Color,
    val background: Color,
    val backgroundLight: Color,
    val backgroundPrimary: Color,
    val backgroundDisabled: Color,
    val backgroundDisabledAlpha: Color,
    val backgroundIcon: Color,
    val backgroundBlack: Color,
)

internal val scannyColors = SCColors(
    transparent = Color.Transparent,
    primary = Color(0xFF3B43BE),
    error = Color(0xFFC53535),
    success = Color(0xFF4BC535),
    textPrimary = Color(0xFF3B43BE),
    text = Color(0xFF666792),
    textDark = Color(0xFF2E2669),
    textLight = Color(0xFFFFFFFF),
    textDisabled = Color(0xFFAAAAAA),
    textBlack = Color(0xFF000000),
    background = Color(0xFFF5F6FC),
    backgroundLight = Color(0xFFFEFEFE),
    backgroundPrimary = Color(0xFF3C44BF),
    backgroundDisabled = Color(0xFFCDCDCD),
    backgroundDisabledAlpha = Color(0xFFCDCDCD).copy(alpha = 0.3f),
    backgroundIcon = Color(0xFFECEDFA),
    backgroundBlack = Color(0xFF000000),
)

internal val LocalColors = staticCompositionLocalOf {
    SCColors(
        transparent = Color.Unspecified,
        primary = Color.Unspecified,
        error = Color.Unspecified,
        success = Color.Unspecified,
        textPrimary = Color.Unspecified,
        text = Color.Unspecified,
        textDark = Color.Unspecified,
        textLight = Color.Unspecified,
        textDisabled = Color.Unspecified,
        textBlack = Color.Unspecified,
        background = Color.Unspecified,
        backgroundLight = Color.Unspecified,
        backgroundPrimary = Color.Unspecified,
        backgroundDisabled = Color.Unspecified,
        backgroundDisabledAlpha = Color.Unspecified,
        backgroundIcon = Color.Unspecified,
        backgroundBlack = Color.Unspecified,
    )
}