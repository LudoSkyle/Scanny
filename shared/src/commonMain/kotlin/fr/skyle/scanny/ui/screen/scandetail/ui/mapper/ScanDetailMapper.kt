package fr.skyle.scanny.ui.screen.scandetail.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.hasDistinctRawValue
import fr.skyle.scanny.ui.core.code.mapper.toCodeAction
import fr.skyle.scanny.ui.core.code.mapper.toCodeFields
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal fun ScannedCode.toScanDetailItem(
    canJoinWifi: (BarcodeContent.Wifi) -> Boolean,
    today: LocalDate,
    timeZone: TimeZone,
): ScanDetailItem =
    ScanDetailItem(
        type = content.toCodeType(),
        format = format,
        scannedDay = scannedAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        ),
        scannedTime = scannedAt.toTimeText(timeZone = timeZone),
        fields = content.toCodeFields(),
        rawValue = rawValue,
        isRawValueShown = content.hasDistinctRawValue(),
        action = content.toCodeAction(canJoinWifi = canJoinWifi),
    )