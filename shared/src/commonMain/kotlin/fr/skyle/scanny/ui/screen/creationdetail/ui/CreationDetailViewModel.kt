package fr.skyle.scanny.ui.screen.creationdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.creation.DeleteCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.SetCreationFavoriteUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCase
import fr.skyle.scanny.platform.image.SavedImage
import fr.skyle.scanny.ui.core.code.image.CodeImageExporter
import fr.skyle.scanny.ui.core.code.model.CodeImageFormat
import fr.skyle.scanny.ui.screen.creationdetail.ui.mapper.toCreationDetailItem
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import kotlin.time.Clock
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

class CreationDetailViewModel(
    private val creationId: Long,
    private val watchCreationUseCase: WatchCreationUseCase,
    private val deleteCreationUseCase: DeleteCreationUseCase,
    private val setCreationFavoriteUseCase: SetCreationFavoriteUseCase,
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

    fun onFavoriteClicked() {
        val creation = _uiState.value.creation ?: return
        viewModelScope.launch {
            try {
                setCreationFavoriteUseCase(
                    creationId = creationId,
                    isFavorite = !creation.isFavorite,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to change the favorite of the creation $creationId" }

                _event.send(CreationDetailEvent.FavoriteNotChanged)
            }
        }
    }

    fun onShareImageClicked(imageFormat: CodeImageFormat) {
        val creation = _uiState.value.creation ?: return
        val format = creation.format ?: return
        viewModelScope.launch {
            try {
                codeImageExporter.share(
                    rawValue = creation.rawValue,
                    format = format,
                    style = creation.style,
                    imageFormat = imageFormat,
                    fileName = imageFileName(imageFormat = imageFormat),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to share the image of the creation $creationId" }

                _event.send(CreationDetailEvent.ImageNotShared)
            }
        }
    }

    fun onSaveImageClicked(imageFormat: CodeImageFormat) {
        val creation = _uiState.value.creation ?: return
        val format = creation.format ?: return
        viewModelScope.launch {
            try {
                val savedImage = codeImageExporter.save(
                    rawValue = creation.rawValue,
                    format = format,
                    style = creation.style,
                    imageFormat = imageFormat,
                    fileName = imageFileName(imageFormat = imageFormat),
                )

                // Null when the user chose not to keep it, iOS asking where: nothing to tell
                savedImage?.let { saved -> _event.send(CreationDetailEvent.ImageSaved(savedImage = saved)) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to save the image of the creation $creationId" }

                _event.send(CreationDetailEvent.ImageNotSaved)
            }
        }
    }

    private fun imageFileName(imageFormat: CodeImageFormat): String =
        "scanny-$creationId.${imageFormat.extension}"

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
        data object FavoriteNotChanged : CreationDetailEvent
        data class ImageSaved(val savedImage: SavedImage) : CreationDetailEvent
        data object ImageNotSaved : CreationDetailEvent
        data object ImageNotShared : CreationDetailEvent
    }
}