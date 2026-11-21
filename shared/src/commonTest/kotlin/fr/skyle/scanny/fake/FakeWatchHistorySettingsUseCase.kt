package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySettings
import fr.skyle.scanny.domain.usecase.history.WatchHistorySettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchHistorySettingsUseCase(
    isScanHistoryEnabled: Boolean = true,
    isCreationHistoryEnabled: Boolean = true,
    isConsecutiveDuplicateSkipped: Boolean = true,
    retention: HistoryRetention = HistoryRetention.ONE_MONTH,
) : WatchHistorySettingsUseCase {

    val settings = MutableStateFlow(
        HistorySettings(
            isScanHistoryEnabled = isScanHistoryEnabled,
            isCreationHistoryEnabled = isCreationHistoryEnabled,
            isConsecutiveDuplicateSkipped = isConsecutiveDuplicateSkipped,
            retention = retention,
        ),
    )

    override fun invoke(): Flow<HistorySettings> =
        settings
}