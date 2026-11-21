package fr.skyle.scanny.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.history.PurgeHistoryUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** What the app does once, at its start, whatever the screen. */
class MainViewModel(
    private val purgeHistoryUseCase: PurgeHistoryUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                purgeHistoryUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Tried again at the next start: the history only stays longer
                Logger.e(throwable = exception) { "Failed to purge the history" }
            }
        }
    }
}