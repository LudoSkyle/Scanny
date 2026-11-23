package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.created_codes_appear_here
import fr.skyle.scanny.resources.creations
import fr.skyle.scanny.resources.ic_qr_code
import fr.skyle.scanny.resources.no_creation_yet
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** What the history shows before the first code created. */
@Composable
fun CreationHistoryEmpty(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_qr_code),
            contentDescription = stringResource(Res.string.creations),
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.no_creation_yet),
            style = SCTheme.typography.h3,
            color = SCTheme.colors.textDark,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.created_codes_appear_here),
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
            textAlign = TextAlign.Center,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationHistoryEmpty() {
    SCTheme {
        CreationHistoryEmpty(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 32.dp),
        )
    }
}
