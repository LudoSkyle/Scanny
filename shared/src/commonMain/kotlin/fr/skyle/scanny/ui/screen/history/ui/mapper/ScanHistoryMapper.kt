package fr.skyle.scanny.ui.screen.history.ui.mapper

import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.ui.core.code.mapper.toCodeSummary
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** The [Scan]s, already from the newest to the oldest, grouped by the day they were scanned. */
internal fun List<Scan>.toScanHistorySections(
    today: LocalDate,
    timeZone: TimeZone,
): List<ScanHistorySection> =
    groupBy { scan ->
        scan.scannedAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        )
    }.map { (day, scans) ->
        ScanHistorySection(
            day = day,
            items = scans.map { scan ->
                scan.toScanHistoryItem(timeZone = timeZone)
            },
        )
    }

private fun Scan.toScanHistoryItem(timeZone: TimeZone): ScanHistoryItem {
    // A capture always holds a code
    val firstCode = codes.first()

    return ScanHistoryItem(
        scanId = id,
        firstCodeId = firstCode.id,
        type = firstCode.content.toCodeType(),
        summary = firstCode.toCodeSummary(),
        time = scannedAt.toTimeText(timeZone = timeZone),
        codeCount = codes.size,
    )
}