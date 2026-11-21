package fr.skyle.scanny.ui.screen.settings.ui.mapper

import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.retention_forever
import fr.skyle.scanny.resources.retention_one_month
import fr.skyle.scanny.resources.retention_one_week
import fr.skyle.scanny.resources.retention_one_year
import fr.skyle.scanny.resources.retention_three_months
import org.jetbrains.compose.resources.StringResource

internal fun HistoryRetention.toText(): StringResource =
    when (this) {
        HistoryRetention.ONE_WEEK -> Res.string.retention_one_week
        HistoryRetention.ONE_MONTH -> Res.string.retention_one_month
        HistoryRetention.THREE_MONTHS -> Res.string.retention_three_months
        HistoryRetention.ONE_YEAR -> Res.string.retention_one_year
        HistoryRetention.FOREVER -> Res.string.retention_forever
    }