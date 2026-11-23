package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.HistorySetting
import fr.skyle.scanny.domain.usecase.history.SetHistorySettingUseCase

/** Records the settings it is given, or fails if [error] is set. */
internal class FakeSetHistorySettingUseCase(
    private val error: Exception? = null,
) : SetHistorySettingUseCase {

    val savedSettings = mutableListOf<Pair<HistorySetting, Boolean>>()

    override suspend fun invoke(
        setting: HistorySetting,
        isEnabled: Boolean,
    ) {
        error?.let { throw it }

        savedSettings += setting to isEnabled
    }
}