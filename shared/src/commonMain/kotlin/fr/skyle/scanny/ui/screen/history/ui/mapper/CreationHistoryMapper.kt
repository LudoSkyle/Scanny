package fr.skyle.scanny.ui.screen.history.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.ui.core.code.mapper.toCodeSummary
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** The [Creation]s, already from the newest to the oldest, grouped by the day they were created. */
internal fun List<Creation>.toCreationHistorySections(
    today: LocalDate,
    timeZone: TimeZone,
): List<CreationHistorySection> =
    groupBy { creation ->
        creation.createdAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        )
    }.map { (day, creations) ->
        CreationHistorySection(
            day = day,
            items = creations.map { creation ->
                CreationHistoryItem(
                    creationId = creation.id,
                    type = creation.content.toCodeType(),
                    format = creation.format,
                    summary = creation.toCodeSummary(),
                    time = creation.createdAt.toTimeText(timeZone = timeZone),
                )
            },
        )
    }
