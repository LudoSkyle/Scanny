package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.fillMaxWidth
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
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.raw_content
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** The text of a code as it is written in it, folded until asked for: few need it. */
@Composable
fun RawContentCard(
    rawValue: String,
    onCopyClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    SCExpandableCardStructure(
        modifier = modifier,
        title = stringResource(Res.string.raw_content),
        isExpanded = isExpanded,
        onExpandedChange = { expanded -> isExpanded = expanded },
        style = SCCardDefaults.Style.LIGHT,
    ) {
        CodeFieldRow(
            modifier = Modifier.fillMaxWidth(),
            label = null,
            value = rawValue,
            onCopyClicked = onCopyClicked,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewRawContentCard() {
    SCTheme {
        RawContentCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            rawValue = "WIFI:T:WPA;S:Scanny;P:secret;;",
            onCopyClicked = {},
        )
    }
}
