package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.week_days
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringArrayResource

/** The days of the week an event repeats on, each switched by a touch, under its [label]. */
@Composable
fun WeekDaysChoice(
    label: String,
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayNames = stringArrayResource(Res.array.week_days)

    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = label,
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
        ) {
            DayOfWeek.entries.forEach { day ->
                val isSelected = day in selectedDays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) SCTheme.colors.primary else SCTheme.colors.backgroundIcon)
                        .toggleable(
                            value = isSelected,
                            role = Role.Checkbox,
                            onValueChange = { isChecked -> onDaysChange(if (isChecked) selectedDays + day else selectedDays - day) },
                        )
                        .semantics { contentDescription = dayNames[day.ordinal] },
                    contentAlignment = Alignment.Center,
                ) {
                    // The first letter of its name, the whole name read by a screen reader
                    SCText(
                        text = dayNames[day.ordinal].take(1).uppercase(),
                        style = SCTheme.typography.button,
                        color = if (isSelected) SCTheme.colors.textLight else SCTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewWeekDaysChoice() {
    SCTheme {
        WeekDaysChoice(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Repeats on",
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            onDaysChange = {},
        )
    }
}
