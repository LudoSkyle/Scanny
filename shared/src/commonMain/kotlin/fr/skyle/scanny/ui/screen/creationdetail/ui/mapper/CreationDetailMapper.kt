package fr.skyle.scanny.ui.screen.creationdetail.ui.mapper

import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.ui.core.code.mapper.toCodeFields
import fr.skyle.scanny.ui.core.code.mapper.toCodeType
import fr.skyle.scanny.ui.core.date.mapper.toCalendarDay
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal fun Creation.toCreationDetailItem(
    today: LocalDate,
    timeZone: TimeZone,
): CreationDetailItem =
    CreationDetailItem(
        type = content.toCodeType(),
        format = format,
        createdDay = createdAt.toCalendarDay(
            today = today,
            timeZone = timeZone,
        ),
        createdTime = createdAt.toTimeText(timeZone = timeZone),
        fields = content.toCodeFields(),
        rawValue = rawValue,
    )