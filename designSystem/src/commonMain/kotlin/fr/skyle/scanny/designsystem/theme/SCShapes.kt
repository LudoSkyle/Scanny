package fr.skyle.scanny.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class SCShapes(
    val medium: Shape,
    val dialog: Shape,
    val sheet: Shape,
)

internal val scannyShapes = SCShapes(
    medium = RoundedCornerShape(size = 10.dp),
    dialog = RoundedCornerShape(size = 24.dp),
    sheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
    ),
)

internal val LocalShapes = staticCompositionLocalOf {
    SCShapes(
        medium = RectangleShape,
        dialog = RectangleShape,
        sheet = RectangleShape,
    )
}