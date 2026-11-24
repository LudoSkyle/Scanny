package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_count
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.ic_stacks
import fr.skyle.scanny.resources.see_group
import fr.skyle.scanny.ui.core.code.component.CodeRow
import fr.skyle.scanny.ui.core.code.component.FavoriteButton
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The codes a multiple scan found: each opens its detail, and the group has its own screen. */
@Composable
fun ScanGroupResultSheet(
    codes: List<CodeRowItem>,
    isFavorite: Boolean,
    onCodeClicked: (codeId: Long) -> Unit,
    onSeeGroupClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    // What came of the star: a message of the screen would show under the sheet
    snackbarState: SCSnackbarState? = null,
) {
    SCBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        snackbarState = snackbarState,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SCIconBadge(
                modifier = Modifier.wrapContentSize(),
                icon = painterResource(Res.drawable.ic_stacks),
                contentDescription = stringResource(Res.string.group),
                style = SCIconBadgeDefaults.Style.PRIMARY,
                size = SCIconBadgeDefaults.Size.LARGE,
            )

            Spacer(modifier = Modifier.width(16.dp))

            SCText(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.code_count, codes.size),
                style = SCTheme.typography.h3,
                color = SCTheme.colors.textDark,
            )

            // The group as a whole
            FavoriteButton(
                modifier = Modifier.wrapContentSize(),
                isFavorite = isFavorite,
                onClick = onFavoriteClicked,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrolls when the codes are more than the sheet can show
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(
                    weight = 1f,
                    fill = false,
                )
                .verticalScroll(state = rememberScrollState()),
        ) {
            codes.forEach { code ->
                CodeRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    item = code,
                    onClick = { onCodeClicked(code.codeId) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SCButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            text = stringResource(Res.string.see_group),
            onClick = onSeeGroupClicked,
            style = SCButtonDefaults.Style.PRIMARY,
            size = SCButtonDefaults.Size.MEDIUM,
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanGroupResultSheet() {
    SCTheme {
        ScanGroupResultSheet(
            modifier = Modifier.fillMaxWidth(),
            codes = listOf(
                CodeRowItem(
                    codeId = 1,
                    type = CodeType.LINK,
                    summary = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                ),
                CodeRowItem(
                    codeId = 2,
                    type = CodeType.WIFI,
                    summary = "Scanny",
                    format = BarcodeFormat.QR_CODE,
                ),
                CodeRowItem(
                    codeId = 3,
                    type = CodeType.TEXT,
                    summary = "4006381333931",
                    format = BarcodeFormat.QR_CODE,
                ),
            ),
            onCodeClicked = {},
            onSeeGroupClicked = {},
            onDismissRequest = {},
            isFavorite = false,
            onFavoriteClicked = {},
        )
    }
}