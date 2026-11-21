package fr.skyle.scanny.ui.screen.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.model.ScanSettings
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
    }

    // Shown again by the settings read back: nothing to update here
    fun onSettingChanged(
        setting: ScanSetting,
        isEnabled: Boolean,
    ) {
        viewModelScope.launch {
            try {
                setScanSettingUseCase(
                    setting = setting,
                    isEnabled = isEnabled,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to save the setting $setting" }

                _event.send(SettingsEvent.SettingNotSaved)
            }
        }
    }

    data class SettingsUIState(
        val versionName: String,
        val build: String,
        // Null while the settings are read
        val settings: ScanSettings? = null,
    )

    sealed interface SettingsEvent {
        data object SettingNotSaved : SettingsEvent
    }
}