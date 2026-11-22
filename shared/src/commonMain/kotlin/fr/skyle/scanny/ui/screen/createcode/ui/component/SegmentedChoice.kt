package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControl
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControlDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground

/** A choice among a few options, side by side under its [label], or alone when its section names it. */
@Composable
fun <T> SegmentedChoice(
    options: List<T>,
    selectedOption: T,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(modifier = modifier) {
        if (label != null) {
            SCText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                text = label,
                style = SCTheme.typography.body2,
                color = SCTheme.colors.text,
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        SCSegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = options,
            selectedOption = selectedOption,
            optionText = optionText,
            onOptionSelected = onOptionSelected,
            style = SCSegmentedControlDefaults.Style.PRIMARY,
            size = SCSegmentedControlDefaults.Size.MEDIUM,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewSegmentedChoice() {
    SCTheme {
        SegmentedChoice(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Gradient",
            options = listOf("None", "Linear", "Radial"),
            selectedOption = "Linear",
            optionText = { option -> option },
            onOptionSelected = {},
        )
    }
}
