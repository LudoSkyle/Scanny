package fr.skyle.scanny.platform.keyboard

import androidx.compose.ui.text.input.KeyboardType

// The decimal pad of iOS has no minus sign: a coordinate south or west would not be typed. The digits
// of the full keyboard have both
internal actual val coordinateKeyboardType: KeyboardType =
    KeyboardType.Ascii