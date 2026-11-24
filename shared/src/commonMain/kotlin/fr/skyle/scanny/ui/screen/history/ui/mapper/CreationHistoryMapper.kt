package fr.skyle.scanny.ui.screen.history.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.ui.core.code.mapper.toCodeSummary
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryHeader
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * The [Creation]s, already from the newest to the oldest: the favorites first, apart, then the others
 * grouped by the day they were created.
 */
internal fun List<Creation>.toCreationHistorySections(
    today: LocalDate,
    timeZone: TimeZone,
): List<CreationHistorySection> {
    val (favorites, others) = partition { creation -> creation.isFavorite }
    val favoriteSection = favorites.takeIf { it.isNotEmpty() }?.let { creations ->
        CreationHistorySection(
            header = HistoryHeader.Favorites,
            items = creations.map { creation ->
                creation.toCreationHistoryItem(timeZone = timeZone)
            },
        )
    }
    val daySections = others.groupBy { creation ->
        creation.createdAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        )
    }.map { (day, creations) ->
        CreationHistorySection(
            header = HistoryHeader.Day(day = day),
            items = creations.map { creation ->
                creation.toCreationHistoryItem(timeZone = timeZone)
            },
        )
    }
    return listOfNotNull(favoriteSection) + daySections
}

private fun Creation.toCreationHistoryItem(timeZone: TimeZone): CreationHistoryItem =
    CreationHistoryItem(
        creationId = id,
        type = content.toCodeType(),
        format = format,
        summary = toCodeSummary(),
        time = createdAt.toTimeText(timeZone = timeZone),
        isFavorite = isFavorite,
    )
