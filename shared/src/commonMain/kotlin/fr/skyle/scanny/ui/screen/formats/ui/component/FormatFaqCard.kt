package fr.skyle.scanny.ui.screen.formats.ui.component

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCExpandableCardStructure
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.format_example
import fr.skyle.scanny.resources.format_read_and_created
import fr.skyle.scanny.resources.format_structure
import fr.skyle.scanny.resources.format_use
import fr.skyle.scanny.ui.core.code.component.CodeInfoSection
import fr.skyle.scanny.ui.core.code.mapper.toExample
import fr.skyle.scanny.ui.core.code.mapper.toStructure
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.mapper.toUse
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/**
 * A format of code, folded under its name: what Scanny does with it, then its use, how it is made and a
 * code of it taken apart, as its sheet in the detail of a code tells them.
 */
@Composable
fun FormatFaqCard(
    format: BarcodeFormat,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    SCExpandableCardStructure(
        modifier = modifier,
        title = stringResource(format.toText()),
        isExpanded = isExpanded,
        onExpandedChange = { expanded -> isExpanded = expanded },
        style = SCCardDefaults.Style.LIGHT,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
        ) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.format_read_and_created),
                style = SCTheme.typography.subtitle2,
                color = SCTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_use),
                text = stringResource(format.toUse()),
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_structure),
                text = stringResource(format.toStructure()),
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_example),
                text = stringResource(format.toExample()),
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewFormatFaqCard() {
    SCTheme {
        FormatFaqCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            format = BarcodeFormat.EAN_13,
        )
    }
}
