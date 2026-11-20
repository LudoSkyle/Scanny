package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.contact
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultType
import org.jetbrains.compose.resources.stringResource

/** The code just scanned: its type and its raw text. */
@Composable
fun ScanResultSheet(
    result: ScanResultItem,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    when (result.type) {
                        ScanResultType.TEXT -> Res.string.text
                        ScanResultType.LINK -> Res.string.link
                        ScanResultType.EMAIL -> Res.string.email
                        ScanResultType.SMS -> Res.string.sms
                        ScanResultType.WIFI -> Res.string.wifi
                        ScanResultType.CONTACT -> Res.string.contact
                    },
                ),
                style = SCTheme.typography.h2,
            )

            Spacer(modifier = Modifier.height(12.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = result.rawValue,
                style = SCTheme.typography.body2,
                color = SCTheme.colors.text,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanResultSheet() {
    SCTheme {
        ScanResultSheet(
            modifier = Modifier.fillMaxWidth(),
            result = ScanResultItem(
                codeId = 1,
                type = ScanResultType.LINK,
                rawValue = "https://scanny.app",
            ),
            onDismissRequest = {},
        )
    }
}