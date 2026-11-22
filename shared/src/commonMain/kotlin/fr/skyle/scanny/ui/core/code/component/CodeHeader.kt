package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.about_format
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
    // What the format is, in a sheet: no "i" when null
    onFormatInfoClicked: (() -> Unit)? = null,
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

                if (onFormatInfoClicked != null) {
                    // The format and its "i" touched as one: a target larger than the icon alone
                    Row(
                        modifier = Modifier
                            .clip(SCTheme.shapes.medium)
                            .clickable(
                                role = Role.Button,
                                onClick = onFormatInfoClicked,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SCText(
                            text = stringResource(format.toText()),
                            style = SCTheme.typography.subtitle2,
                            color = SCTheme.colors.text,
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Image(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.ic_info),
                            contentDescription = stringResource(Res.string.about_format),
                            colorFilter = ColorFilter.tint(color = SCTheme.colors.text),
                        )
                    }
                } else {
                    SCText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(format.toText()),
                        style = SCTheme.typography.subtitle2,
                        color = SCTheme.colors.text,
                    )
                }
            }
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