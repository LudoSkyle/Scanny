package fr.skyle.scanny.ui.screen.scangroup.ui.mapper

import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.ui.core.code.mapper.toCodeRowItem
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.scangroup.ui.model.ScanGroupItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal fun Scan.toScanGroupItem(
    today: LocalDate,
    timeZone: TimeZone,
): ScanGroupItem =
    ScanGroupItem(
        scannedDay = scannedAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        ),
        scannedTime = scannedAt.toTimeText(timeZone = timeZone),
        codes = codes.map { code ->
            code.toCodeRowItem()
        },
    )