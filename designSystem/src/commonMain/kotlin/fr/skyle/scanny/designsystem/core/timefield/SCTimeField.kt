package fr.skyle.scanny.designsystem.core.timefield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.textfield.SCTextField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.cancel
import fr.skyle.scanny.designsystem.resources.clear
import fr.skyle.scanny.designsystem.resources.ok
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.stringResource

private const val MINUTES_PER_HOUR = 60

/**
 * A time the user picks on a clock, named by its [label], shown as [timeText], the caller writing it.
 * [selectedMinutes] is the time in minutes since midnight; [onTimeSelected] gives the one picked back, or
 * null once cleared.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCTimeField(
    label: String,
    timeText: String,
    selectedMinutes: Int?,
    onTimeSelected: (Int?) -> Unit,
    style: SCTextFieldDefaults.Style,
    modifier: Modifier = Modifier,
) {
    var isClockVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Shown only: the button over it is what a screen reader reads
        SCTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {},
            value = timeText,
            onValueChange = {},
            label = label,
            style = style,
            isSingleLine = true,
        )
        // Over the field: a touch opens the clock, not the keyboard
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp)
                .clip(SCTheme.shapes.medium)
                .clickable(
                    role = Role.Button,
                    onClick = { isClockVisible = true },
                )
                .semantics {
                    contentDescription = label
                    stateDescription = timeText
                },
        )
    }

    if (isClockVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = (selectedMinutes ?: 0) / MINUTES_PER_HOUR,
            initialMinute = (selectedMinutes ?: 0) % MINUTES_PER_HOUR,
        )

        TimePickerDialog(
            onDismissRequest = { isClockVisible = false },
            title = {
                SCText(
                    text = label,
                    style = SCTheme.typography.body2,
                    color = SCTheme.colors.text,
                )
            },
            confirmButton = {
                DialogButton(
                    text = stringResource(Res.string.ok),
                    onClick = {
                        onTimeSelected(timePickerState.hour * MINUTES_PER_HOUR + timePickerState.minute)
                        isClockVisible = false
                    },
                )
            },
            dismissButton = {
                Row {
                    DialogButton(
                        text = stringResource(Res.string.clear),
                        onClick = {
                            onTimeSelected(null)
                            isClockVisible = false
                        },
                    )
                    DialogButton(
                        text = stringResource(Res.string.cancel),
                        onClick = { isClockVisible = false },
                    )
                }
            },
            containerColor = SCTheme.colors.backgroundLight,
        ) {
            TimePicker(
                state = timePickerState,
                colors = timePickerColors(),
            )
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        SCText(
            text = text,
            style = SCTheme.typography.button,
            color = SCTheme.colors.primary,
        )
    }
}

// The colors of the theme, rather than the ones of Material, which the theme does not set
@Composable
private fun timePickerColors(): TimePickerColors =
    TimePickerDefaults.colors(
        clockDialColor = SCTheme.colors.backgroundIcon,
        clockDialSelectedContentColor = SCTheme.colors.textLight,
        clockDialUnselectedContentColor = SCTheme.colors.textDark,
        selectorColor = SCTheme.colors.primary,
        containerColor = SCTheme.colors.backgroundLight,
        periodSelectorBorderColor = SCTheme.colors.backgroundDisabled,
        periodSelectorSelectedContainerColor = SCTheme.colors.backgroundIcon,
        periodSelectorUnselectedContainerColor = SCTheme.colors.transparent,
        periodSelectorSelectedContentColor = SCTheme.colors.textPrimary,
        periodSelectorUnselectedContentColor = SCTheme.colors.text,
        timeSelectorSelectedContainerColor = SCTheme.colors.backgroundIcon,
        timeSelectorUnselectedContainerColor = SCTheme.colors.background,
        timeSelectorSelectedContentColor = SCTheme.colors.textPrimary,
        timeSelectorUnselectedContentColor = SCTheme.colors.textDark,
    )

@Preview(showBackground = true)
@Composable
private fun PreviewSCTimeField() {
    SCTheme {
        SCTimeField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Start",
            timeText = "15:00",
            selectedMinutes = 900,
            onTimeSelected = {},
            style = SCTextFieldDefaults.Style.LIGHT,
        )
    }
}
