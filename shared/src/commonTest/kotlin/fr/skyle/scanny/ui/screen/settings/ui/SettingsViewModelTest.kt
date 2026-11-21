package fr.skyle.scanny.ui.screen.settings.ui

import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.fake.FakeAppVersion
import fr.skyle.scanny.fake.FakeSetScanSettingUseCase
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
                isOpenLinkAfterScanEnabled = false,
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
                    isOpenLinkAfterScanEnabled = false,
                    isRawContentShown = true,
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `the settings follow their changes`() = runTest(testDispatcher) {
        // Given
        val watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isOpenLinkAfterScanEnabled = false)
        val viewModel = settingsViewModel(watchScanSettingsUseCase = watchScanSettingsUseCase)

        // When
        watchScanSettingsUseCase.settings.value = watchScanSettingsUseCase.settings.value.copy(isOpenLinkAfterScanEnabled = true)

        // Then
        assertEquals(
            expected = true,
            actual = viewModel.uiState.value.settings?.isOpenLinkAfterScanEnabled,
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

    private fun settingsViewModel(
        watchScanSettingsUseCase: FakeWatchScanSettingsUseCase = FakeWatchScanSettingsUseCase(),
        setScanSettingUseCase: FakeSetScanSettingUseCase = FakeSetScanSettingUseCase(),
    ): SettingsViewModel =
        SettingsViewModel(
            watchScanSettingsUseCase = watchScanSettingsUseCase,
            setScanSettingUseCase = setScanSettingUseCase,
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