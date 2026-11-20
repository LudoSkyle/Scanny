package fr.skyle.scanny.designsystem.core.screenstructure

import androidx.compose.runtime.Composable

// The color of the status and navigation bar icons, which only the platform knows how to change
@Composable
internal expect fun SystemBarIcons(areLight: Boolean)