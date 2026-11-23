package fr.skyle.scanny.ui.screen.formats.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.barcodes
import fr.skyle.scanny.resources.code_formats
import fr.skyle.scanny.resources.formats_intro
import fr.skyle.scanny.resources.two_dimensional_codes
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.formats.ui.component.FormatFaqCard
import fr.skyle.scanny.ui.screen.formats.ui.mapper.isTwoDimensional
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Every format of code Scanny reads, the codes in two dimensions then the barcodes, each folded. */
@Composable
fun FormatsScreen(
    onBackClicked: () -> Unit,
) {
    val (twoDimensionalFormats, barcodeFormats) = BarcodeFormat.entries.partition { format -> format.isTwoDimensional }

    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.code_formats),
                onBackClicked = onBackClicked,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(
                key = "intro",
                contentType = "intro",
            ) {
                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 8.dp,
                        ),
                    text = stringResource(Res.string.formats_intro),
                    style = SCTheme.typography.body2,
                    color = SCTheme.colors.text,
                )
            }

            formatSection(
                key = "two_dimensional",
                title = Res.string.two_dimensional_codes,
                formats = twoDimensionalFormats,
            )

            formatSection(
                key = "barcodes",
                title = Res.string.barcodes,
                formats = barcodeFormats,
            )

            item(
                key = "end",
                contentType = "end",
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// A title, then the formats under it
private fun LazyListScope.formatSection(
    key: String,
    title: StringResource,
    formats: List<BarcodeFormat>,
) {
    item(
        key = key,
        contentType = "title",
    ) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
                .padding(
                    start = 20.dp,
                    top = 16.dp,
                    end = 20.dp,
                    bottom = 8.dp,
                ),
            text = stringResource(title),
            style = SCTheme.typography.subtitle1,
            color = SCTheme.colors.text,
        )
    }

    items(
        items = formats,
        key = { format -> format.name },
        contentType = { "format" },
    ) { format ->
        FormatFaqCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp,
                ),
            format = format,
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewFormatsScreen() {
    SCTheme {
        FormatsScreen(onBackClicked = {})
    }
}
