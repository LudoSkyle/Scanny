package fr.skyle.scanny.designsystem.core.colorpicker

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SCColorPickerDefaults {

    enum class Size {
        MEDIUM;

        // Of the square of saturation and brightness, wider than high
        val areaAspectRatio: Float
            get() = when (this) {
                MEDIUM -> 1.6f
            }

        val hueBarHeight: Dp
            get() = when (this) {
                MEDIUM -> 28.dp
            }

        val thumbSize: Dp
            get() = when (this) {
                MEDIUM -> 24.dp
            }
    }
}
