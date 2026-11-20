package fr.skyle.scanny.ui.screen.about.ui

import androidx.lifecycle.ViewModel
import fr.skyle.scanny.platform.app.AppVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AboutViewModel(
    appVersion: AppVersion,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AboutUIState(
            versionName = appVersion.name,
            build = appVersion.build,
        ),
    )
    val uiState: StateFlow<AboutUIState> = _uiState.asStateFlow()

    data class AboutUIState(
        val versionName: String,
        val build: String,
    )
}