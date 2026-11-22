package fr.skyle.scanny.ui.screen.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySettings
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.app_name
import fr.skyle.scanny.resources.beep_after_scan
import fr.skyle.scanny.resources.give_feedback
import fr.skyle.scanny.resources.keep_history
import fr.skyle.scanny.resources.rate_app
import fr.skyle.scanny.resources.retention_forever
import fr.skyle.scanny.resources.retention_one_month
import fr.skyle.scanny.resources.scan_preferences
import fr.skyle.scanny.resources.version_and_build
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class SettingsScreenTest : ComposeTest() {

    @Test
    fun `the settings show the app and its version`() = runComposeUiTest {
        // When
        setContent {
            settingsScreen()
        }

        // Then
        onNodeWithText(getString(Res.string.app_name)).assertExists()
        onNodeWithText(getString(Res.string.version_and_build, "2.0", "20")).assertExists()
        onNodeWithText(getString(Res.string.scan_preferences)).assertExists()
    }

    @Test
    fun `a setting touched is turned on or off`() = runComposeUiTest {
        // Given
        val changes = mutableListOf<Pair<ScanSetting, Boolean>>()
        setContent {
            settingsScreen(
                onSettingChanged = { setting, isEnabled -> changes += setting to isEnabled },
            )
        }

        // When
        onNodeWithText(getString(Res.string.beep_after_scan)).performClick()

        // Then
        assertEquals(
            expected = listOf(ScanSetting.BEEP_AFTER_SCAN to false),
            actual = changes,
        )
    }

    @Test
    fun `the feedback is given from its row and the app is not rated before the store`() = runComposeUiTest {
        // Given
        var feedbackCount = 0
        setContent {
            settingsScreen(onFeedbackClicked = { feedbackCount++ })
        }

        // When
        onNodeWithText(getString(Res.string.give_feedback)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.rate_app)).assertDoesNotExist()
        assertEquals(
            expected = 1,
            actual = feedbackCount,
        )
    }

    @Test
    fun `the retention of the history is chosen in a menu`() = runComposeUiTest {
        // Given
        val retentions = mutableListOf<HistoryRetention>()
        setContent {
            settingsScreen(onHistoryRetentionChanged = { retention -> retentions += retention })
        }
        onNodeWithText(getString(Res.string.retention_one_month)).performScrollTo().assertExists()

        // When
        onNodeWithText(getString(Res.string.keep_history)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.retention_forever)).performClick()

        // Then
        assertEquals(
            expected = listOf(HistoryRetention.FOREVER),
            actual = retentions,
        )
    }

    @Composable
    private fun settingsScreen(
        onSettingChanged: (ScanSetting, Boolean) -> Unit = { _, _ -> },
        onHistoryRetentionChanged: (HistoryRetention) -> Unit = {},
        onFeedbackClicked: () -> Unit = {},
    ) {
        SCTheme {
            SettingsScreen(
                settings = ScanSettings(
                    isVibrationAfterScanEnabled = true,
                    isBeepAfterScanEnabled = true,
                    isCopyAfterScanEnabled = false,
                    isOpenContentAfterScanEnabled = false,
                    isFrontCameraUsed = false,
                ),
                historySettings = HistorySettings(
                    isScanHistoryEnabled = true,
                    isCreationHistoryEnabled = true,
                    isConsecutiveDuplicateSkipped = true,
                    retention = HistoryRetention.ONE_MONTH,
                ),
                versionName = "2.0",
                build = "20",
                isRateAppAvailable = false,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onSettingChanged = onSettingChanged,
                onHistorySettingChanged = { _, _ -> },
                onHistoryRetentionChanged = onHistoryRetentionChanged,
                onStarProjectClicked = {},
                onFeedbackClicked = onFeedbackClicked,
                onRateAppClicked = {},
                onPrivacyPolicyClicked = {},
                onOpeniumClicked = {},
            )
        }
    }
}
