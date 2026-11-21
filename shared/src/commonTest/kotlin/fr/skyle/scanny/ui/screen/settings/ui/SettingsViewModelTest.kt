package fr.skyle.scanny.ui.screen.settings.ui

import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySetting
import fr.skyle.scanny.domain.model.HistorySettings
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.fake.FakeAppVersion
import fr.skyle.scanny.fake.FakeSetHistoryRetentionUseCase
import fr.skyle.scanny.fake.FakeSetHistorySettingUseCase
import fr.skyle.scanny.fake.FakeSetScanSettingUseCase
import fr.skyle.scanny.fake.FakeWatchHistorySettingsUseCase
import fr.skyle.scanny.fake.FakeWatchScanSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the settings and the version of the app are shown`() = runTest(testDispatcher) {
        // When
        val viewModel = settingsViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = true,
                isBeepAfterScanEnabled = true,
                isCopyAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = false,
                isRawContentShown = true,
            ),
        )

        // Then
        assertEquals(
            expected = SettingsViewModel.SettingsUIState(
                versionName = "2.1",
                build = "21",
                settings = ScanSettings(
                    isVibrationAfterScanEnabled = true,
                    isBeepAfterScanEnabled = true,
                    isCopyAfterScanEnabled = false,
                    isOpenContentAfterScanEnabled = false,
                    isRawContentShown = true,
                ),
                historySettings = HistorySettings(
                    isScanHistoryEnabled = true,
                    isConsecutiveDuplicateSkipped = true,
                    retention = HistoryRetention.ONE_MONTH,
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `the settings follow their changes`() = runTest(testDispatcher) {
        // Given
        val watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isOpenContentAfterScanEnabled = false)
        val viewModel = settingsViewModel(watchScanSettingsUseCase = watchScanSettingsUseCase)

        // When
        watchScanSettingsUseCase.settings.value = watchScanSettingsUseCase.settings.value.copy(isOpenContentAfterScanEnabled = true)

        // Then
        assertEquals(
            expected = true,
            actual = viewModel.uiState.value.settings?.isOpenContentAfterScanEnabled,
        )
    }

    @Test
    fun `a changed setting is saved`() = runTest(testDispatcher) {
        // Given
        val setScanSettingUseCase = FakeSetScanSettingUseCase()
        val viewModel = settingsViewModel(setScanSettingUseCase = setScanSettingUseCase)

        // When
        viewModel.onSettingChanged(
            setting = ScanSetting.RAW_CONTENT_SHOWN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(ScanSetting.RAW_CONTENT_SHOWN to true),
            actual = setScanSettingUseCase.savedSettings,
        )
    }

    @Test
    fun `a setting that cannot be saved is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = settingsViewModel(setScanSettingUseCase = FakeSetScanSettingUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSettingChanged(
            setting = ScanSetting.VIBRATION_AFTER_SCAN,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(SettingsViewModel.SettingsEvent.SettingNotSaved),
            actual = events,
        )
    }

    @Test
    fun `the settings of the history follow their changes`() = runTest(testDispatcher) {
        // Given
        val watchHistorySettingsUseCase = FakeWatchHistorySettingsUseCase()
        val viewModel = settingsViewModel(watchHistorySettingsUseCase = watchHistorySettingsUseCase)

        // When
        watchHistorySettingsUseCase.settings.value = watchHistorySettingsUseCase.settings.value.copy(retention = HistoryRetention.FOREVER)

        // Then
        assertEquals(
            expected = HistoryRetention.FOREVER,
            actual = viewModel.uiState.value.historySettings?.retention,
        )
    }

    @Test
    fun `a changed setting of the history is saved`() = runTest(testDispatcher) {
        // Given
        val setHistorySettingUseCase = FakeSetHistorySettingUseCase()
        val viewModel = settingsViewModel(setHistorySettingUseCase = setHistorySettingUseCase)

        // When
        viewModel.onHistorySettingChanged(
            setting = HistorySetting.SCAN_HISTORY,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(HistorySetting.SCAN_HISTORY to false),
            actual = setHistorySettingUseCase.savedSettings,
        )
    }

    @Test
    fun `a chosen retention is saved`() = runTest(testDispatcher) {
        // Given
        val setHistoryRetentionUseCase = FakeSetHistoryRetentionUseCase()
        val viewModel = settingsViewModel(setHistoryRetentionUseCase = setHistoryRetentionUseCase)

        // When
        viewModel.onHistoryRetentionChanged(retention = HistoryRetention.ONE_WEEK)

        // Then
        assertEquals(
            expected = listOf(HistoryRetention.ONE_WEEK),
            actual = setHistoryRetentionUseCase.savedRetentions,
        )
    }

    @Test
    fun `a retention that cannot be saved is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = settingsViewModel(setHistoryRetentionUseCase = FakeSetHistoryRetentionUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onHistoryRetentionChanged(retention = HistoryRetention.ONE_YEAR)

        // Then
        assertEquals(
            expected = listOf(SettingsViewModel.SettingsEvent.SettingNotSaved),
            actual = events,
        )
    }

    private fun settingsViewModel(
        watchScanSettingsUseCase: FakeWatchScanSettingsUseCase = FakeWatchScanSettingsUseCase(),
        setScanSettingUseCase: FakeSetScanSettingUseCase = FakeSetScanSettingUseCase(),
        watchHistorySettingsUseCase: FakeWatchHistorySettingsUseCase = FakeWatchHistorySettingsUseCase(),
        setHistorySettingUseCase: FakeSetHistorySettingUseCase = FakeSetHistorySettingUseCase(),
        setHistoryRetentionUseCase: FakeSetHistoryRetentionUseCase = FakeSetHistoryRetentionUseCase(),
    ): SettingsViewModel =
        SettingsViewModel(
            watchScanSettingsUseCase = watchScanSettingsUseCase,
            setScanSettingUseCase = setScanSettingUseCase,
            watchHistorySettingsUseCase = watchHistorySettingsUseCase,
            setHistorySettingUseCase = setHistorySettingUseCase,
            setHistoryRetentionUseCase = setHistoryRetentionUseCase,
            appVersion = FakeAppVersion(
                name = "2.1",
                build = "21",
            ),
        )

    private fun TestScope.collectEvents(viewModel: SettingsViewModel): List<SettingsViewModel.SettingsEvent> {
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }
}