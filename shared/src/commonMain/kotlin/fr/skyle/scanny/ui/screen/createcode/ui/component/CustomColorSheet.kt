package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.colorpicker.SCColorPicker
import fr.skyle.scanny.designsystem.core.colorpicker.SCColorPickerDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.choose
import fr.skyle.scanny.resources.choose_color
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.HsvColor
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toArgb
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toHsvColor
import org.jetbrains.compose.resources.stringResource

/** Any color for a code, out of the palette, chosen on a picker from [initialArgb], kept on "Choose". */
@Composable
fun CustomColorSheet(
    initialArgb: Long,
    onColorChosen: (argb: Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // In hue, saturation and brightness while it is picked: a grey keeps its hue when it is lightened
    var color by remember(initialArgb) { mutableStateOf(initialArgb.toHsvColor()) }

    SCBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SCText(
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    text = stringResource(Res.string.choose_color),
                    style = SCTheme.typography.h3,
                    color = SCTheme.colors.textDark,
                )

                Spacer(modifier = Modifier.width(16.dp))

                // The color as it is chosen
                Spacer(
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            width = 1.dp,
                            color = SCTheme.colors.backgroundDisabled,
                            shape = CircleShape,
                        )
                        .background(
                            color = Color(color = color.toArgb()),
                            shape = CircleShape,
                        ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SCColorPicker(
                modifier = Modifier.fillMaxWidth(),
                hue = color.hue,
                saturation = color.saturation,
                value = color.value,
                onColorChange = { hue, saturation, value ->
                    color = HsvColor(
                        hue = hue,
                        saturation = saturation,
                        value = value,
                    )
                },
                size = SCColorPickerDefaults.Size.MEDIUM,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.choose),
                onClick = { onColorChosen(color.toArgb()) },
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCustomColorSheet() {
    SCTheme {
        CustomColorSheet(
            initialArgb = 0xFF00897B,
            onColorChosen = {},
            onDismissRequest = {},
        )
    }
}
