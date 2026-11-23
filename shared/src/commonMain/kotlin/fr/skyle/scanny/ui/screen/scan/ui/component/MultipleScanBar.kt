package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.codes_found
import fr.skyle.scanny.resources.finish
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A multiple scan in progress: the codes found so far, and the button that ends it. */
@Composable
fun MultipleScanBar(
    codeCount: Int,
    isRecording: Boolean,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCCard(
        modifier = modifier,
        style = SCCardDefaults.Style.LIGHT,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SCText(
                modifier = Modifier.weight(1f),
                text = pluralStringResource(Res.plurals.codes_found, codeCount, codeCount),
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
            )

            Spacer(modifier = Modifier.width(12.dp))

            SCButton(
                modifier = Modifier.wrapContentWidth(),
                text = stringResource(Res.string.finish),
                onClick = onFinishClicked,
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
                isEnabled = codeCount > 0 && !isRecording,
            )
        }
    }
}

@AppPreview
@Composable
private fun PreviewMultipleScanBar() {
    SCTheme {
        MultipleScanBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            codeCount = 3,
            isRecording = false,
            onFinishClicked = {},
        )
    }
}

@AppPreview
@Composable
private fun PreviewMultipleScanBar_NoCodeYet() {
    SCTheme {
        MultipleScanBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            codeCount = 0,
            isRecording = false,
            onFinishClicked = {},
        )
    }
}