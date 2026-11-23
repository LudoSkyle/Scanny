package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.textfield.SCTextField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.color_component_blue
import fr.skyle.scanny.resources.color_component_green
import fr.skyle.scanny.resources.color_component_red
import fr.skyle.scanny.resources.color_hex
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.blue
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.green
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.hexToArgb
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.red
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.rgbToArgb
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toHex
import org.jetbrains.compose.resources.stringResource

private const val HEX_LENGTH = 6
private const val COMPONENT_LENGTH = 3

/**
 * The color typed as its hexadecimal code or as its red, green and blue, from 0 to 255. A color
 * changes once complete and valid, and the field typed in keeps what is typed while the others follow.
 */
@Composable
fun ColorCodeFields(
    argb: Long,
    onArgbChanged: (argb: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hexText by remember { mutableStateOf(argb.toHex()) }
    var redText by remember { mutableStateOf(argb.red.toString()) }
    var greenText by remember { mutableStateOf(argb.green.toString()) }
    var blueText by remember { mutableStateOf(argb.blue.toString()) }

    // The color changed by the picker or by another field: a field that does not say it any more follows
    LaunchedEffect(argb) {
        if (hexText.hexToArgb() != argb) {
            hexText = argb.toHex()
        }
        if (redText.toIntOrNull() != argb.red) {
            redText = argb.red.toString()
        }
        if (greenText.toIntOrNull() != argb.green) {
            greenText = argb.green.toString()
        }
        if (blueText.toIntOrNull() != argb.blue) {
            blueText = argb.blue.toString()
        }
    }

    fun onComponentChanged() {
        val red = redText.toIntOrNull() ?: return
        val green = greenText.toIntOrNull() ?: return
        val blue = blueText.toIntOrNull() ?: return
        rgbToArgb(red = red, green = green, blue = blue)?.let(onArgbChanged)
    }

    Column(modifier = modifier) {
        SCTextField(
            modifier = Modifier.fillMaxWidth(),
            value = hexText,
            onValueChange = { text ->
                // The # typed or pasted with the digits is dropped
                hexText = text.removePrefix("#").uppercase().filter { digit -> digit.isDigit() || digit in 'A'..'F' }.take(HEX_LENGTH)
                hexText.hexToArgb()?.let(onArgbChanged)
            },
            label = stringResource(Res.string.color_hex),
            style = SCTextFieldDefaults.Style.LIGHT,
            isSingleLine = true,
            keyboardType = KeyboardType.Ascii,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Under the code: next to it, a field would be too narrow for its name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SCTextField(
                modifier = Modifier.weight(1f),
                value = redText,
                onValueChange = { text ->
                    redText = text.filter(Char::isDigit).take(COMPONENT_LENGTH)
                    onComponentChanged()
                },
                label = stringResource(Res.string.color_component_red),
                style = SCTextFieldDefaults.Style.LIGHT,
                isSingleLine = true,
                keyboardType = KeyboardType.Number,
            )

            SCTextField(
                modifier = Modifier.weight(1f),
                value = greenText,
                onValueChange = { text ->
                    greenText = text.filter(Char::isDigit).take(COMPONENT_LENGTH)
                    onComponentChanged()
                },
                label = stringResource(Res.string.color_component_green),
                style = SCTextFieldDefaults.Style.LIGHT,
                isSingleLine = true,
                keyboardType = KeyboardType.Number,
            )

            SCTextField(
                modifier = Modifier.weight(1f),
                value = blueText,
                onValueChange = { text ->
                    blueText = text.filter(Char::isDigit).take(COMPONENT_LENGTH)
                    onComponentChanged()
                },
                label = stringResource(Res.string.color_component_blue),
                style = SCTextFieldDefaults.Style.LIGHT,
                isSingleLine = true,
                keyboardType = KeyboardType.Number,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewColorCodeFields() {
    SCTheme {
        ColorCodeFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            argb = 0xFF3B43BE,
            onArgbChanged = {},
        )
    }
}
