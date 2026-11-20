package fr.skyle.scanny.ui.screen.scandetail.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.toCodeAction
import fr.skyle.scanny.ui.core.code.mapper.toCodeFields
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal fun ScannedCode.toScanDetailItem(
    isRawContentShown: Boolean,
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
        // A text or a link is its own raw value, already shown
        isRawValueShown = isRawContentShown && content !is BarcodeContent.Text && content !is BarcodeContent.Url,
        action = content.toCodeAction(canJoinWifi = canJoinWifi),
    )