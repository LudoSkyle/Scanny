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
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySetting
import fr.skyle.scanny.domain.model.HistorySettings
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.app_settings
import fr.skyle.scanny.resources.beep_after_scan
import fr.skyle.scanny.resources.copy_after_scan
import fr.skyle.scanny.resources.give_feedback
import fr.skyle.scanny.resources.history
import fr.skyle.scanny.resources.ic_auto_delete
import fr.skyle.scanny.resources.ic_cameraswitch
import fr.skyle.scanny.resources.ic_chat
import fr.skyle.scanny.resources.ic_chevron_right
import fr.skyle.scanny.resources.ic_content_copy
import fr.skyle.scanny.resources.ic_control_point_duplicate
import fr.skyle.scanny.resources.ic_data_object
import fr.skyle.scanny.resources.ic_domain
import fr.skyle.scanny.resources.ic_history
import fr.skyle.scanny.resources.ic_open_in_new
import fr.skyle.scanny.resources.ic_shield
import fr.skyle.scanny.resources.ic_star
import fr.skyle.scanny.resources.ic_thumb_up
import fr.skyle.scanny.resources.ic_vibration
import fr.skyle.scanny.resources.ic_volume_up
import fr.skyle.scanny.resources.keep_history
import fr.skyle.scanny.resources.made_by_skyle
import fr.skyle.scanny.resources.open_content_after_scan
import fr.skyle.scanny.resources.openium
import fr.skyle.scanny.resources.privacy_policy
import fr.skyle.scanny.resources.rate_app
import fr.skyle.scanny.resources.save_scans_in_history
import fr.skyle.scanny.resources.scan_preferences
import fr.skyle.scanny.resources.settings
import fr.skyle.scanny.resources.show_raw_content
import fr.skyle.scanny.resources.skip_consecutive_duplicates
import fr.skyle.scanny.resources.star_project
import fr.skyle.scanny.resources.support
import fr.skyle.scanny.resources.use_front_camera
import fr.skyle.scanny.resources.vibrate_after_scan
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsActionRow
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsChoiceRow
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsHeader
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsRowDivider
import fr.skyle.scanny.ui.screen.settings.ui.component.SettingsSwitchRow
import fr.skyle.scanny.ui.screen.settings.ui.mapper.toText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    settings: ScanSettings?,
    historySettings: HistorySettings?,
    versionName: String,
    build: String,
    isRateAppAvailable: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onSettingChanged: (setting: ScanSetting, isEnabled: Boolean) -> Unit,
    onHistorySettingChanged: (setting: HistorySetting, isEnabled: Boolean) -> Unit,
    onHistoryRetentionChanged: (retention: HistoryRetention) -> Unit,
    onStarProjectClicked: () -> Unit,
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
            SettingsHeader(
                modifier = Modifier.fillMaxWidth(),
                versionName = versionName,
                build = build,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (settings != null) {
                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.scan_preferences),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_vibration),
                        title = stringResource(Res.string.vibrate_after_scan),
                        isChecked = settings.isVibrationAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.VIBRATION_AFTER_SCAN, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_volume_up),
                        title = stringResource(Res.string.beep_after_scan),
                        isChecked = settings.isBeepAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.BEEP_AFTER_SCAN, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_content_copy),
                        title = stringResource(Res.string.copy_after_scan),
                        isChecked = settings.isCopyAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.COPY_AFTER_SCAN, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_open_in_new),
                        title = stringResource(Res.string.open_content_after_scan),
                        isChecked = settings.isOpenContentAfterScanEnabled,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.OPEN_CONTENT_AFTER_SCAN, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_cameraswitch),
                        title = stringResource(Res.string.use_front_camera),
                        isChecked = settings.isFrontCameraUsed,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.FRONT_CAMERA, isEnabled) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (historySettings != null) {
                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.history),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_history),
                        title = stringResource(Res.string.save_scans_in_history),
                        isChecked = historySettings.isScanHistoryEnabled,
                        onCheckedChange = { isEnabled -> onHistorySettingChanged(HistorySetting.SCAN_HISTORY, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_control_point_duplicate),
                        title = stringResource(Res.string.skip_consecutive_duplicates),
                        isChecked = historySettings.isConsecutiveDuplicateSkipped,
                        onCheckedChange = { isEnabled -> onHistorySettingChanged(HistorySetting.CONSECUTIVE_DUPLICATE_SKIPPED, isEnabled) },
                    )

                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsChoiceRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_auto_delete),
                        title = stringResource(Res.string.keep_history),
                        options = HistoryRetention.entries,
                        selectedOption = historySettings.retention,
                        optionText = { retention -> stringResource(retention.toText()) },
                        onOptionSelected = onHistoryRetentionChanged,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (settings != null) {
                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.app_settings),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    SettingsSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_data_object),
                        title = stringResource(Res.string.show_raw_content),
                        isChecked = settings.isRawContentShown,
                        onCheckedChange = { isEnabled -> onSettingChanged(ScanSetting.RAW_CONTENT_SHOWN, isEnabled) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            SCCardStructure(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.support),
                style = SCCardDefaults.Style.LIGHT,
            ) {
                SettingsActionRow(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(Res.drawable.ic_star),
                    title = stringResource(Res.string.star_project),
                    trailingIcon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onStarProjectClicked,
                )

                SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                SettingsActionRow(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(Res.drawable.ic_chat),
                    title = stringResource(Res.string.give_feedback),
                    trailingIcon = painterResource(Res.drawable.ic_chevron_right),
                    onClick = onFeedbackClicked,
                )

                if (isRateAppAvailable) {
                    SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                    SettingsActionRow(
                        modifier = Modifier.fillMaxWidth(),
                        icon = painterResource(Res.drawable.ic_thumb_up),
                        title = stringResource(Res.string.rate_app),
                        trailingIcon = painterResource(Res.drawable.ic_open_in_new),
                        onClick = onRateAppClicked,
                    )
                }

                SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                SettingsActionRow(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(Res.drawable.ic_shield),
                    title = stringResource(Res.string.privacy_policy),
                    trailingIcon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onPrivacyPolicyClicked,
                )

                SettingsRowDivider(modifier = Modifier.fillMaxWidth())

                SettingsActionRow(
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(Res.drawable.ic_domain),
                    title = stringResource(Res.string.openium),
                    trailingIcon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onOpeniumClicked,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.made_by_skyle),
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
                isBeepAfterScanEnabled = false,
                isCopyAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = false,
                isRawContentShown = false,
                isFrontCameraUsed = false,
            ),
            historySettings = HistorySettings(
                isScanHistoryEnabled = true,
                isConsecutiveDuplicateSkipped = true,
                retention = HistoryRetention.ONE_MONTH,
            ),
            versionName = "2.0",
            build = "20",
            isRateAppAvailable = true,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onSettingChanged = { _, _ -> },
            onHistorySettingChanged = { _, _ -> },
            onHistoryRetentionChanged = {},
            onStarProjectClicked = {},
            onFeedbackClicked = {},
            onRateAppClicked = {},
            onPrivacyPolicyClicked = {},
            onOpeniumClicked = {},
        )
    }
}