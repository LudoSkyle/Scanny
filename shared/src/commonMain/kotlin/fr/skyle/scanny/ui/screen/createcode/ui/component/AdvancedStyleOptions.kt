package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCExpandableCardStructure
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.advanced_options
import fr.skyle.scanny.resources.corner_color
import fr.skyle.scanny.resources.corner_color_like_dots
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CornerColorOption
import org.jetbrains.compose.resources.stringResource

/**
 * The customization few need, folded under its title: the color of the three corners, the one of the
 * dots unless chosen apart.
 */
@Composable
fun AdvancedStyleOptions(
    style: CodeStyle,
    onStyleChanged: (CodeStyle) -> Unit,
    onCustomCornerColorClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val cornerColorOptions = listOf(CornerColorOption.LikeDots) + CodeDotColor.entries.map(CornerColorOption::Palette)
    // A color out of the palette shows on the last disc
    val customCornerColor = style.cornerColor?.takeIf { argb -> CodeDotColor.entries.none { color -> color.argb == argb } }

    SCExpandableCardStructure(
        modifier = modifier,
        title = stringResource(Res.string.advanced_options),
        isExpanded = isExpanded,
        onExpandedChange = { expanded -> isExpanded = expanded },
        style = SCCardDefaults.Style.LIGHT,
    ) {
        ColorChoiceRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.corner_color),
            options = cornerColorOptions,
            selectedOption = when (val cornerColor = style.cornerColor) {
                null -> CornerColorOption.LikeDots
                else -> CodeDotColor.entries.firstOrNull { color -> color.argb == cornerColor }?.let(CornerColorOption::Palette)
            },
            optionColor = { option ->
                when (option) {
                    CornerColorOption.LikeDots -> Color(color = style.darkColor)
                    is CornerColorOption.Palette -> Color(color = option.color.argb)
                }
            },
            optionName = { option ->
                when (option) {
                    CornerColorOption.LikeDots -> stringResource(Res.string.corner_color_like_dots)
                    is CornerColorOption.Palette -> stringResource(option.color.toText())
                }
            },
            onOptionSelected = { option ->
                onStyleChanged(
                    style.copy(
                        cornerColor = when (option) {
                            CornerColorOption.LikeDots -> null
                            is CornerColorOption.Palette -> option.color.argb
                        },
                    ),
                )
            },
            customColor = customCornerColor?.let { argb -> Color(color = argb) },
            onCustomColorClicked = onCustomCornerColorClicked,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewAdvancedStyleOptions() {
    SCTheme {
        AdvancedStyleOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            style = CodeStyle(cornerColor = CodeDotColor.RED.argb),
            onStyleChanged = {},
            onCustomCornerColorClicked = {},
        )
    }
}
