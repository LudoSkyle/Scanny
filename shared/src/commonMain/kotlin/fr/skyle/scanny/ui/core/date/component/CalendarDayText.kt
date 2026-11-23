package fr.skyle.scanny.ui.core.date.component

import androidx.compose.runtime.Composable
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.day_date
import fr.skyle.scanny.resources.months
import fr.skyle.scanny.resources.today
import fr.skyle.scanny.resources.yesterday
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** The name of a day, in the language of the device: kotlinx-datetime does not translate months. */
@Composable
internal fun calendarDayText(day: CalendarDay): String =
    when (day) {
        CalendarDay.Today -> stringResource(Res.string.today)
        CalendarDay.Yesterday -> stringResource(Res.string.yesterday)
        is CalendarDay.Date -> stringResource(
            Res.string.day_date,
            day.date.day,
            stringArrayResource(Res.array.months)[day.date.month.ordinal],
            day.date.year,
        )
    }