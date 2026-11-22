package fr.skyle.scanny.platform.keyboard

import androidx.compose.ui.text.input.KeyboardType

// The decimal keypad of Android has a minus sign
internal actual val coordinateKeyboardType: KeyboardType =
    KeyboardType.Decimal