package fr.skyle.scanny.ui.screen.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySetting
import fr.skyle.scanny.domain.model.HistorySettings
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.domain.usecase.history.SetHistoryRetentionUseCase
import fr.skyle.scanny.domain.usecase.history.SetHistorySettingUseCase
import fr.skyle.scanny.domain.usecase.history.WatchHistorySettingsUseCase
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.platform.app.AppVersion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val watchScanSettingsUseCase: WatchScanSettingsUseCase,
    private val setScanSettingUseCase: SetScanSettingUseCase,
    private val watchHistorySettingsUseCase: WatchHistorySettingsUseCase,
    private val setHistorySettingUseCase: SetHistorySettingUseCase,
    private val setHistoryRetentionUseCase: SetHistoryRetentionUseCase,
    appVersion: AppVersion,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUIState(
            versionName = appVersion.name,
            build = appVersion.build,
        ),
    )
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    private val _event = Channel<SettingsEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchScanSettingsUseCase()
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the settings" }
                }
                .collect { settings ->
                    _uiState.update { uiState ->
                        uiState.copy(settings = settings)
                    }
                }
        }
        viewModelScope.launch {
            watchHistorySettingsUseCase()
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the settings of the history" }
                }
                .collect { historySettings ->
                    _uiState.update { uiState ->
                        uiState.copy(historySettings = historySettings)
                    }
                }
        }
    }

    fun onSettingChanged(
        setting: ScanSetting,
        isEnabled: Boolean,
    ) {
        save(name = "the setting $setting") {
            setScanSettingUseCase(
                setting = setting,
                isEnabled = isEnabled,
            )
        }
    }

    fun onHistorySettingChanged(
        setting: HistorySetting,
        isEnabled: Boolean,
    ) {
        save(name = "the setting $setting") {
            setHistorySettingUseCase(
                setting = setting,
                isEnabled = isEnabled,
            )
        }
    }

    fun onHistoryRetentionChanged(retention: HistoryRetention) {
        save(name = "the retention $retention") {
            setHistoryRetentionUseCase(retention = retention)
        }
    }

    // Shown again by the settings read back: nothing to update here
    private fun save(
        name: String,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to save $name" }

                _event.send(SettingsEvent.SettingNotSaved)
            }
        }
    }

    data class SettingsUIState(
        val versionName: String,
        val build: String,
        // Null while the settings are read
        val settings: ScanSettings? = null,
        val historySettings: HistorySettings? = null,
    )

    sealed interface SettingsEvent {
        data object SettingNotSaved : SettingsEvent
    }
}