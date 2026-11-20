package fr.skyle.scanny.ui.screen.scanhistory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import fr.skyle.scanny.ui.screen.scanhistory.ui.mapper.toScanHistorySections
import fr.skyle.scanny.ui.screen.scanhistory.ui.model.ScanHistorySection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ScanHistoryViewModel(
    private val watchScansUseCase: WatchScansUseCase,
    private val deleteAllScansUseCase: DeleteAllScansUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanHistoryUIState())
    val uiState: StateFlow<ScanHistoryUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanHistoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchScansUseCase()
                .map { scans ->
                    scans.toScanHistorySections(
                        today = clock.now().toLocalDateTime(timeZone).date,
                        timeZone = timeZone,
                    )
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the history of the scans" }
                }
                .collect { sections ->
                    _uiState.update { uiState ->
                        uiState.copy(sections = sections)
                    }
                }
        }
    }

    fun onClearConfirmed() {
        viewModelScope.launch {
            try {
                deleteAllScansUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to clear the history of the scans" }

                _event.send(ScanHistoryEvent.ClearFailed)
            }
        }
    }

    data class ScanHistoryUIState(
        // Null while the history is read
        val sections: List<ScanHistorySection>? = null,
    )

    sealed interface ScanHistoryEvent {
        data object ClearFailed : ScanHistoryEvent
    }
}