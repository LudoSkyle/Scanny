package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.background_color
import fr.skyle.scanny.resources.corner_shape
import fr.skyle.scanny.resources.customize_code
import fr.skyle.scanny.resources.dot_color
import fr.skyle.scanny.resources.dot_shape
import fr.skyle.scanny.ui.core.code.mapper.readability
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import org.jetbrains.compose.resources.stringResource

/** The colors and the shapes of a QR code, chosen among those any scanner reads, and its logo. */
@Composable
fun CodeStyleFields(
    style: CodeStyle,
    onStyleChanged: (CodeStyle) -> Unit,
    onAddLogoClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The color being picked out of the palette, with what it is applied to
    var customColorTarget by remember { mutableStateOf<CustomColorTarget?>(null) }
    // A color out of the palette shows on the last disc
    val customDotColor = style.darkColor.takeIf { argb -> CodeDotColor.entries.none { color -> color.argb == argb } }
    val customBackgroundColor = style.lightColor.takeIf { argb -> CodeBackgroundColor.entries.none { color -> color.argb == argb } }

    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
                .padding(horizontal = 4.dp),
            text = stringResource(Res.string.customize_code),
            style = SCTheme.typography.subtitle1,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ColorChoiceRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.dot_color),
            options = CodeDotColor.entries,
            selectedOption = CodeDotColor.entries.firstOrNull { color -> color.argb == style.darkColor },
            optionColor = { color -> Color(color = color.argb) },
            optionName = { color -> stringResource(color.toText()) },
            onOptionSelected = { color -> onStyleChanged(style.copy(darkColor = color.argb)) },
            customColor = customDotColor?.let { argb -> Color(color = argb) },
            onCustomColorClicked = { customColorTarget = CustomColorTarget.DOTS },
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorChoiceRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.background_color),
            options = CodeBackgroundColor.entries,
            selectedOption = CodeBackgroundColor.entries.firstOrNull { color -> color.argb == style.lightColor },
            optionColor = { color -> Color(color = color.argb) },
            optionName = { color -> stringResource(color.toText()) },
            onOptionSelected = { color -> onStyleChanged(style.copy(lightColor = color.argb)) },
            customColor = customBackgroundColor?.let { argb -> Color(color = argb) },
            onCustomColorClicked = { customColorTarget = CustomColorTarget.BACKGROUND },
        )

        CodeReadabilityWarning(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    top = 12.dp,
                    end = 4.dp,
                ),
            readability = style.readability(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        SCDropdownField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.dot_shape),
            placeholder = stringResource(Res.string.dot_shape),
            options = CodeShape.entries,
            selectedOption = style.dotShape,
            optionText = { shape -> stringResource(shape.toText()) },
            onOptionSelected = { shape -> onStyleChanged(style.copy(dotShape = shape)) },
            style = SCTextFieldDefaults.Style.LIGHT,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SCDropdownField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.corner_shape),
            placeholder = stringResource(Res.string.corner_shape),
            options = CodeShape.entries,
            selectedOption = style.cornerShape,
            optionText = { shape -> stringResource(shape.toText()) },
            onOptionSelected = { shape -> onStyleChanged(style.copy(cornerShape = shape)) },
            style = SCTextFieldDefaults.Style.LIGHT,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LogoRow(
            modifier = Modifier.fillMaxWidth(),
            logo = style.logo,
            onAddLogoClicked = onAddLogoClicked,
            onRemoveLogoClicked = { onStyleChanged(style.copy(logo = null)) },
        )
    }

    customColorTarget?.let { target ->
        CustomColorSheet(
            initialArgb = when (target) {
                CustomColorTarget.DOTS -> style.darkColor
                CustomColorTarget.BACKGROUND -> style.lightColor
            },
            onColorChosen = { argb ->
                customColorTarget = null
                onStyleChanged(
                    when (target) {
                        CustomColorTarget.DOTS -> style.copy(darkColor = argb)
                        CustomColorTarget.BACKGROUND -> style.copy(lightColor = argb)
                    },
                )
            },
            onDismissRequest = { customColorTarget = null },
        )
    }
}

private enum class CustomColorTarget {
    DOTS,
    BACKGROUND,
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeStyleFields() {
    SCTheme {
        CodeStyleFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            style = CodeStyle(
                darkColor = CodeDotColor.INDIGO.argb,
                dotShape = CodeShape.ROUNDED,
            ),
            onStyleChanged = {},
            onAddLogoClicked = {},
        )
    }
}
