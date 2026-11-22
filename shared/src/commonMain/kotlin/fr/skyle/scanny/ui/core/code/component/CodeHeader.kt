package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.about_code
import fr.skyle.scanny.resources.ic_info
import fr.skyle.scanny.ui.core.code.mapper.toIcon
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The type of a code, drawn and named, and its format when this version knows it. */
@Composable
fun CodeHeader(
    type: CodeType,
    format: BarcodeFormat?,
    modifier: Modifier = Modifier,
    // What the type and the format are, in a sheet: none when null
    onInfoClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(type.toIcon()),
            contentDescription = stringResource(type.toText()),
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(type.toText()),
                style = SCTheme.typography.h3,
                color = SCTheme.colors.textDark,
            )

            if (format != null) {
                Spacer(modifier = Modifier.height(2.dp))

                SCText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(format.toText()),
                    style = SCTheme.typography.subtitle2,
                    color = SCTheme.colors.text,
                )
            }
        }

        if (onInfoClicked != null) {
            Spacer(modifier = Modifier.width(8.dp))

            SCIconButton(
                modifier = Modifier.wrapContentSize(),
                icon = painterResource(Res.drawable.ic_info),
                contentDescription = stringResource(Res.string.about_code),
                onClick = onInfoClicked,
                style = SCIconButtonDefaults.Style.DARK,
                size = SCIconButtonDefaults.Size.MEDIUM,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeHeader() {
    SCTheme {
        CodeHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            type = CodeType.WIFI,
            format = BarcodeFormat.QR_CODE,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeHeader_UnknownFormat() {
    SCTheme {
        CodeHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            type = CodeType.TEXT,
            format = null,
        )
    }
}