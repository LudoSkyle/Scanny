package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.advanced_options
import fr.skyle.scanny.resources.corner_color
import fr.skyle.scanny.resources.corner_color_like_dots
import fr.skyle.scanny.resources.gradient
import fr.skyle.scanny.resources.gradient_color
import fr.skyle.scanny.resources.logo_margin
import fr.skyle.scanny.resources.logo_shape
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CornerColorOption
import org.jetbrains.compose.resources.stringResource

/**
 * The customization few need, folded under its title: the color of the three corners, the one of the
 * dots unless chosen apart, a gradient of the dots to a second color, and the shape and the margin of
 * the logo, once there is one.
 */
@Composable
fun AdvancedStyleOptions(
    style: CodeStyle,
    onStyleChanged: (CodeStyle) -> Unit,
    onCustomCornerColorClicked: () -> Unit,
    onCustomGradientColorClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    // The color of the dots is offered once, as theirs: a disc of the palette would look the same
    val cornerColorOptions = listOf(CornerColorOption.LikeDots) +
        CodeDotColor.entries.filter { color -> color.argb != style.darkColor }.map(CornerColorOption::Palette)
    val cornerColor = style.cornerColor?.takeIf { argb -> argb != style.darkColor }
    // A color out of the palette shows on the last disc
    val customCornerColor = cornerColor?.takeIf { argb -> CodeDotColor.entries.none { color -> color.argb == argb } }
    val customGradientColor = style.gradientColor.takeIf { argb -> CodeDotColor.entries.none { color -> color.argb == argb } }

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
            selectedOption = when (cornerColor) {
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

        Spacer(modifier = Modifier.height(16.dp))

        SegmentedChoice(
            label = stringResource(Res.string.gradient),
            options = CodeGradient.entries,
            selectedOption = style.gradient,
            optionText = { gradient -> stringResource(gradient.toText()) },
            onOptionSelected = { gradient -> onStyleChanged(style.copy(gradient = gradient)) },
        )

        // The color the dots go to, from their own
        if (style.gradient != CodeGradient.NONE) {
            Spacer(modifier = Modifier.height(16.dp))

            ColorChoiceRow(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.gradient_color),
                options = CodeDotColor.entries,
                selectedOption = CodeDotColor.entries.firstOrNull { color -> color.argb == style.gradientColor },
                optionColor = { color -> Color(color = color.argb) },
                optionName = { color -> stringResource(color.toText()) },
                onOptionSelected = { color -> onStyleChanged(style.copy(gradientColor = color.argb)) },
                customColor = customGradientColor?.let { argb -> Color(color = argb) },
                onCustomColorClicked = onCustomGradientColorClicked,
            )
        }

        if (style.logo != null) {
            Spacer(modifier = Modifier.height(16.dp))

            SegmentedChoice(
                label = stringResource(Res.string.logo_shape),
                options = CodeShape.entries,
                selectedOption = style.logoShape,
                optionText = { shape -> stringResource(shape.toText()) },
                onOptionSelected = { shape -> onStyleChanged(style.copy(logoShape = shape)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SegmentedChoice(
                label = stringResource(Res.string.logo_margin),
                options = CodeLogoMargin.entries,
                selectedOption = style.logoMargin,
                optionText = { margin -> stringResource(margin.toText()) },
                onOptionSelected = { margin -> onStyleChanged(style.copy(logoMargin = margin)) },
            )
        }
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
            style = CodeStyle(
                cornerColor = CodeDotColor.RED.argb,
                gradient = CodeGradient.LINEAR,
                logo = byteArrayOf(1, 2, 3),
                logoShape = CodeShape.CIRCLE,
            ),
            onStyleChanged = {},
            onCustomCornerColorClicked = {},
            onCustomGradientColorClicked = {},
        )
    }
}
