package fr.skyle.scanny.ui.screen.scangroup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCase
import fr.skyle.scanny.domain.usecase.scan.SetScanFavoriteUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCase
import fr.skyle.scanny.platform.share.TextSharer
import fr.skyle.scanny.ui.screen.scangroup.ui.mapper.toScanGroupItem
import fr.skyle.scanny.ui.screen.scangroup.ui.model.ScanGroupItem
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ScanGroupViewModel(
    private val scanId: Long,
    private val watchScanUseCase: WatchScanUseCase,
    private val deleteScanUseCase: DeleteScanUseCase,
    private val setScanFavoriteUseCase: SetScanFavoriteUseCase,
    private val textSharer: TextSharer,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanGroupUIState())
    val uiState: StateFlow<ScanGroupUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanGroupEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var scan: Scan? = null

    init {
        viewModelScope.launch {
            watchScanUseCase(scanId = scanId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the group $scanId" }
                }
                .collect { watchedScan ->
                    // Deleted here, or emptied of its codes from their detail: the screen has nothing left
                    if (watchedScan == null && scan != null) {
                        _event.send(ScanGroupEvent.GroupGone)
                    }

                    scan = watchedScan
                    _uiState.update { uiState ->
                        uiState.copy(
                            group = watchedScan?.toScanGroupItem(
                                today = clock.now().toLocalDateTime(timeZone).date,
                                timeZone = timeZone,
                            ),
                        )
                    }
                }
        }
    }

    // The group as a whole
    fun onFavoriteClicked() {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            try {
                setScanFavoriteUseCase(
                    scanId = scanId,
                    isFavorite = !group.isFavorite,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to change the favorite of the group $scanId" }

                _event.send(ScanGroupEvent.FavoriteNotChanged)
            }
        }
    }

    // One code per line: what another app, or a scanner, reads back
    fun onShareClicked() {
        val codes = scan?.codes ?: return
        textSharer.shareText(
            text = codes.joinToString(separator = "\n") { code ->
                code.rawValue
            },
        )
    }

    // Once deleted, the group is no longer watched: the screen closes on that
    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                deleteScanUseCase(scanId = scanId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to delete the group $scanId" }

                _event.send(ScanGroupEvent.DeleteFailed)
            }
        }
    }

    data class ScanGroupUIState(
        val group: ScanGroupItem? = null,
    )

    sealed interface ScanGroupEvent {
        data object GroupGone : ScanGroupEvent
        data object DeleteFailed : ScanGroupEvent
        data object FavoriteNotChanged : ScanGroupEvent
    }
}