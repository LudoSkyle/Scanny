package fr.skyle.scanny.ui.screen.creationdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.creation.DeleteCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCase
import fr.skyle.scanny.ui.core.code.image.CodeImageExporter
import fr.skyle.scanny.ui.screen.creationdetail.ui.mapper.toCreationDetailItem
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
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

class CreationDetailViewModel(
    private val creationId: Long,
    private val watchCreationUseCase: WatchCreationUseCase,
    private val deleteCreationUseCase: DeleteCreationUseCase,
    private val codeImageExporter: CodeImageExporter,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreationDetailUIState(canSaveImage = codeImageExporter.canSave))
    val uiState: StateFlow<CreationDetailUIState> = _uiState.asStateFlow()

    private val _event = Channel<CreationDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchCreationUseCase(creationId = creationId)
                .map { creation ->
                    creation?.toCreationDetailItem(
                        today = clock.now().toLocalDateTime(timeZone).date,
                        timeZone = timeZone,
                    )
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the creation $creationId" }
                }
                .collect { creation ->
                    _uiState.update { uiState ->
                        uiState.copy(creation = creation)
                    }
                }
        }
    }

    fun onShareImageClicked() {
        val creation = _uiState.value.creation ?: return
        val format = creation.format ?: return
        viewModelScope.launch {
            try {
                codeImageExporter.share(
                    rawValue = creation.rawValue,
                    format = format,
                    style = creation.style,
                    fileName = imageFileName(),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to share the image of the creation $creationId" }

                _event.send(CreationDetailEvent.ImageNotShared)
            }
        }
    }

    fun onSaveImageClicked() {
        val creation = _uiState.value.creation ?: return
        val format = creation.format ?: return
        viewModelScope.launch {
            try {
                codeImageExporter.save(
                    rawValue = creation.rawValue,
                    format = format,
                    style = creation.style,
                    fileName = imageFileName(),
                )

                _event.send(CreationDetailEvent.ImageSaved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to save the image of the creation $creationId" }

                _event.send(CreationDetailEvent.ImageNotSaved)
            }
        }
    }

    private fun imageFileName(): String =
        "scanny-$creationId.png"

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isDeleting = true)
            }

            try {
                deleteCreationUseCase(creationId = creationId)

                _event.send(CreationDetailEvent.CreationDeleted)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to delete the creation $creationId" }

                _event.send(CreationDetailEvent.DeleteFailed)
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isDeleting = false)
                }
            }
        }
    }

    data class CreationDetailUIState(
        val canSaveImage: Boolean,
        val creation: CreationDetailItem? = null,
        val isDeleting: Boolean = false,
    )

    sealed interface CreationDetailEvent {
        data object CreationDeleted : CreationDetailEvent
        data object DeleteFailed : CreationDetailEvent
        data object ImageSaved : CreationDetailEvent
        data object ImageNotSaved : CreationDetailEvent
        data object ImageNotShared : CreationDetailEvent
    }
}