package fr.skyle.scanny.ui.screen.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCCardStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.about
import fr.skyle.scanny.resources.app_name
import fr.skyle.scanny.resources.feedback
import fr.skyle.scanny.resources.general
import fr.skyle.scanny.resources.ic_chevron_right
import fr.skyle.scanny.resources.ic_open_in_new
import fr.skyle.scanny.resources.open_links_after_scan
import fr.skyle.scanny.resources.openium
import fr.skyle.scanny.resources.privacy_policy
import fr.skyle.scanny.resources.rate_app
import fr.skyle.scanny.resources.settings
import fr.skyle.scanny.resources.show_raw_content
import fr.skyle.scanny.resources.version_and_build
import fr.skyle.scanny.resources.vibrate_after_scan
import fr.skyle.scanny.ui.core.link.component.LinkRow
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsSwitchRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    settings: ScanSettings?,
    versionName: String,
    build: String,
    isRateAppAvailable: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onSettingChanged: (setting: ScanSetting, isEnabled: Boolean) -> Unit,
    onAboutClicked: () -> Unit,
    onFeedbackClicked: () -> Unit,
    onRateAppClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onOpeniumClicked: () -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.settings),
                onBackClicked = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        ) {
            if (settings != null) {
                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.general),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.vibrate_after_scan),
                        isChecked = settings.isVibrationAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.VIBRATION_AFTER_SCAN, isEnabled) },
                    )

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.open_links_after_scan),
                        isChecked = settings.isOpenLinkAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.OPEN_LINK_AFTER_SCAN, isEnabled) },
                    )

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.show_raw_content),
                        isChecked = settings.isRawContentShown,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.RAW_CONTENT_SHOWN, isEnabled) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            SCCardStructure(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.app_name),
                style = SCCardDefaults.Style.LIGHT,
            ) {
                LinkRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.about),
                    icon = painterResource(Res.drawable.ic_chevron_right),
                    onClick = onAboutClicked,
                )

                LinkRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.feedback),
                    icon = painterResource(Res.drawable.ic_chevron_right),
                    onClick = onFeedbackClicked,
                )

                if (isRateAppAvailable) {
                    LinkRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.rate_app),
                        icon = painterResource(Res.drawable.ic_open_in_new),
                        onClick = onRateAppClicked,
                    )
                }

                LinkRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.privacy_policy),
                    icon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onPrivacyPolicyClicked,
                )

                LinkRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.openium),
                    icon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onOpeniumClicked,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.version_and_build, versionName, build),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewSettingsScreen() {
    SCTheme {
        SettingsScreen(
            settings = ScanSettings(
                isVibrationAfterScanEnabled = true,
                isOpenLinkAfterScanEnabled = false,
                isRawContentShown = false,
            ),
            versionName = "2.0",
            build = "20",
            isRateAppAvailable = true,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onSettingChanged = { _, _ -> },
            onAboutClicked = {},
            onFeedbackClicked = {},
            onRateAppClicked = {},
            onPrivacyPolicyClicked = {},
            onOpeniumClicked = {},
        )
    }
}