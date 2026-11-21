package fr.skyle.scanny.designsystem.core.colorpicker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.hue
import fr.skyle.scanny.designsystem.resources.saturation_brightness
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.stringResource

// Every hue around the circle, red at both ends
private val HUES = listOf(0f, 60f, 120f, 180f, 240f, 300f, 0f).map { hue -> Color.hsv(hue = hue, saturation = 1f, value = 1f) }

/**
 * Any color, chosen by its hue on a bar, then by its saturation and its brightness on a square of that
 * hue: a touch or a drag on either. [hue] goes from 0 to 360, [saturation] and [value] from 0 to 1.
 */
@Composable
fun SCColorPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (hue: Float, saturation: Float, value: Float) -> Unit,
    size: SCColorPickerDefaults.Size,
    modifier: Modifier = Modifier,
) {
    // Read by the gestures, which live longer than a composition
    val currentHue by rememberUpdatedState(hue)
    val currentSaturation by rememberUpdatedState(saturation)
    val currentValue by rememberUpdatedState(value)
    val currentOnColorChange by rememberUpdatedState(onColorChange)

    Column(modifier = modifier) {
        val areaDescription = stringResource(Res.string.saturation_brightness)
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(size.areaAspectRatio)
                .clip(SCTheme.shapes.medium)
                .semantics { contentDescription = areaDescription }
                .pointerInput(Unit) {
                    detectTapGestures { position ->
                        currentOnColorChange(currentHue, this.size.fractionX(position), 1f - this.size.fractionY(position))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        currentOnColorChange(currentHue, this.size.fractionX(change.position), 1f - this.size.fractionY(change.position))
                    }
                }
                .drawBehind {
                    drawRect(brush = Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue = hue, saturation = 1f, value = 1f))))
                    drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    drawThumb(
                        center = Offset(x = saturation * this.size.width, y = (1f - value) * this.size.height),
                        diameter = size.thumbSize.toPx(),
                    )
                },
        )

        Spacer(modifier = Modifier.height(16.dp))

        val hueDescription = stringResource(Res.string.hue)
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.hueBarHeight)
                .clip(CircleShape)
                .semantics { contentDescription = hueDescription }
                .pointerInput(Unit) {
                    detectTapGestures { position ->
                        currentOnColorChange(this.size.fractionX(position) * 360f, currentSaturation, currentValue)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        currentOnColorChange(this.size.fractionX(change.position) * 360f, currentSaturation, currentValue)
                    }
                }
                .drawBehind {
                    drawRect(brush = Brush.horizontalGradient(HUES))
                    drawThumb(
                        center = Offset(x = hue / 360f * this.size.width, y = this.size.height / 2f),
                        diameter = size.thumbSize.toPx(),
                    )
                },
        )
    }
}

// Where a touch falls across a width or a height, from 0 to 1, a drag out of it held at its edge
private fun IntSize.fractionX(position: Offset): Float =
    (position.x / width).coerceIn(0f, 1f)

private fun IntSize.fractionY(position: Offset): Float =
    (position.y / height).coerceIn(0f, 1f)

// A ring seen on any color: white, edged with a shadow
private fun DrawScope.drawThumb(
    center: Offset,
    diameter: Float,
) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = diameter / 2f + 1.dp.toPx(),
        center = center,
        style = Stroke(width = 1.dp.toPx()),
    )
    drawCircle(
        color = Color.White,
        radius = diameter / 2f,
        center = center,
        style = Stroke(width = 3.dp.toPx()),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCColorPicker() {
    SCTheme {
        var hue by remember { mutableFloatStateOf(240f) }
        var saturation by remember { mutableFloatStateOf(0.7f) }
        var value by remember { mutableFloatStateOf(0.75f) }
        SCColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            hue = hue,
            saturation = saturation,
            value = value,
            onColorChange = { newHue, newSaturation, newValue ->
                hue = newHue
                saturation = newSaturation
                value = newValue
            },
            size = SCColorPickerDefaults.Size.MEDIUM,
        )
    }
}
