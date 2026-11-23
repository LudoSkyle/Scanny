package fr.skyle.scanny.designsystem.core.datefield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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

/**
 * A date the user picks in a calendar, named by its [label], shown as [dateText], the caller writing it
 * in its language. [selectedDateMillis] is the date at midnight UTC, as the calendar reads it;
 * [onDateSelected] gives the one picked back, or null once cleared.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCDateField(
    label: String,
    dateText: String,
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    style: SCTextFieldDefaults.Style,
    modifier: Modifier = Modifier,
) {
    var isCalendarVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Shown only: the button over it is what a screen reader reads
        SCTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {},
            value = dateText,
            onValueChange = {},
            label = label,
            style = style,
            isSingleLine = true,
        )
        // Over the field: a touch opens the calendar, not the keyboard
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp)
                .clip(SCTheme.shapes.medium)
                .clickable(
                    role = Role.Button,
                    onClick = { isCalendarVisible = true },
                )
                .semantics {
                    contentDescription = label
                    stateDescription = dateText
                },
        )
    }

    if (isCalendarVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        val colors = datePickerColors()

        DatePickerDialog(
            onDismissRequest = { isCalendarVisible = false },
            confirmButton = {
                DialogButton(
                    text = stringResource(Res.string.ok),
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        isCalendarVisible = false
                    },
                )
            },
            dismissButton = {
                Row {
                    DialogButton(
                        text = stringResource(Res.string.clear),
                        onClick = {
                            onDateSelected(null)
                            isCalendarVisible = false
                        },
                    )
                    DialogButton(
                        text = stringResource(Res.string.cancel),
                        onClick = { isCalendarVisible = false },
                    )
                }
            },
            colors = colors,
        ) {
            DatePicker(
                state = datePickerState,
                colors = colors,
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun datePickerColors(): DatePickerColors =
    DatePickerDefaults.colors(
        containerColor = SCTheme.colors.backgroundLight,
        titleContentColor = SCTheme.colors.text,
        headlineContentColor = SCTheme.colors.textDark,
        weekdayContentColor = SCTheme.colors.text,
        subheadContentColor = SCTheme.colors.text,
        navigationContentColor = SCTheme.colors.textDark,
        yearContentColor = SCTheme.colors.textDark,
        currentYearContentColor = SCTheme.colors.primary,
        selectedYearContentColor = SCTheme.colors.textLight,
        selectedYearContainerColor = SCTheme.colors.primary,
        dayContentColor = SCTheme.colors.textDark,
        selectedDayContentColor = SCTheme.colors.textLight,
        selectedDayContainerColor = SCTheme.colors.primary,
        todayContentColor = SCTheme.colors.primary,
        todayDateBorderColor = SCTheme.colors.primary,
        dividerColor = SCTheme.colors.backgroundDisabled,
    )

@Preview(showBackground = true)
@Composable
private fun PreviewSCDateField() {
    SCTheme {
        SCDateField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Birthday",
            dateText = "12 May 1990",
            selectedDateMillis = null,
            onDateSelected = {},
            style = SCTextFieldDefaults.Style.LIGHT,
        )
    }
}
