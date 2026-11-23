package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.platform.wifi.WifiJoinResult
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_to_calendar
import fr.skyle.scanny.resources.call
import fr.skyle.scanny.resources.copied
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.date_at_time
import fr.skyle.scanny.resources.day_date
import fr.skyle.scanny.resources.every_weeks
import fr.skyle.scanny.resources.minutes_before
import fr.skyle.scanny.resources.months
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.recurrence_with
import fr.skyle.scanny.resources.see_detail
import fr.skyle.scanny.resources.status_confirmed
import fr.skyle.scanny.resources.time_in_zone
import fr.skyle.scanny.resources.times_count
import fr.skyle.scanny.resources.upc_a
import fr.skyle.scanny.resources.week_days
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.wifi_saved
import fr.skyle.scanny.resources.write_sms
import fr.skyle.scanny.ui.core.code.mapper.showWifiJoinResult
import fr.skyle.scanny.ui.core.code.mapper.toCodeFields
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import fr.skyle.scanny.ui.test.ComposeTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getStringArray
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ScanResultSheetTest : ComposeTest() {

    @Test
    fun `an sms shows its number and its message and is written by its button`() = runComposeUiTest {
        // Given
        val actions = mutableListOf<CodeAction>()
        setContent {
            scanResultSheet(
                result = SMS,
                onActionClicked = { action -> actions += action },
            )
        }

        // When
        onNodeWithText(getString(Res.string.write_sms)).performClick()

        // Then
        onNodeWithText("+33600000000").assertExists()
        onNodeWithText("See you at 10:30 & bring été").assertExists()
        assertEquals(
            expected = listOf(SMS.action),
            actual = actions,
        )
    }

    @Test
    fun `a phone shows its number and is called by its button`() = runComposeUiTest {
        // Given
        val actions = mutableListOf<CodeAction>()
        setContent {
            scanResultSheet(
                result = PHONE,
                onActionClicked = { action -> actions += action },
            )
        }

        // When
        onNodeWithText(getString(Res.string.call)).performClick()

        // Then
        onNodeWithText(getString(Res.string.phone)).assertExists()
        onNodeWithText("+33600000000").assertExists()
        assertEquals(
            expected = listOf(PHONE.action),
            actual = actions,
        )
    }

    @Test
    fun `an event writes its dates its recurrence and its reminder in the language of the device`() = runComposeUiTest {
        // Given
        val event = BarcodeContent.Event(
            summary = "Launch",
            start = EventDateTime(date = LocalDate(2026, 11, 22), time = LocalTime(15, 0), timeZone = "Europe/Paris"),
            recurrence = EventRecurrence(frequency = EventFrequency.WEEKLY, interval = 2, count = 10, days = setOf(DayOfWeek.MONDAY)),
            reminderMinutes = 15,
            status = EventStatus.CONFIRMED,
        )

        // When
        setContent {
            scanResultSheet(
                result = ScanResultItem(
                    codeId = 5,
                    type = CodeType.EVENT,
                    format = BarcodeFormat.QR_CODE,
                    fields = event.toCodeFields(),
                    rawValue = "BEGIN:VEVENT",
                    action = CodeAction.AddEvent(event = event),
                ),
            )
        }

        // Then
        val day = getString(Res.string.day_date, 22, getStringArray(Res.array.months)[10], 2026)
        val start = getString(Res.string.time_in_zone, getString(Res.string.date_at_time, day, "15:00"), "Europe/Paris")
        val every = getString(Res.string.recurrence_with, getPluralString(Res.plurals.every_weeks, 2, 2), getStringArray(Res.array.week_days)[0])
        onNodeWithText(start).assertExists()
        onNodeWithText("$every, ${getPluralString(Res.plurals.times_count, 10, 10)}").assertExists()
        onNodeWithText(getPluralString(Res.plurals.minutes_before, 15, 15)).assertExists()
        onNodeWithText(getString(Res.string.status_confirmed)).assertExists()
        onNodeWithText(getString(Res.string.add_to_calendar)).assertExists()
    }

    @Test
    fun `the code is copied and its detail opened from the sheet`() = runComposeUiTest {
        // Given
        val clicks = mutableListOf<String>()
        setContent {
            scanResultSheet(
                result = SMS,
                onCopyClicked = { clicks += "copy" },
                onSeeDetailClicked = { clicks += "detail" },
            )
        }

        // When
        onNodeWithText(getString(Res.string.copy)).performClick()
        onNodeWithText(getString(Res.string.see_detail)).performClick()

        // Then
        // iOS shows nothing when a text is copied: the button tells it
        onNodeWithText(getString(Res.string.copied)).assertExists()
        assertEquals(
            expected = listOf("copy", "detail"),
            actual = clicks,
        )
    }

    @Test
    fun `a barcode shows its format`() = runComposeUiTest {
        // When
        setContent {
            scanResultSheet(
                result = ScanResultItem(
                    codeId = 2,
                    type = CodeType.TEXT,
                    format = BarcodeFormat.UPC_A,
                    fields = listOf(
                        CodeField(
                            label = null,
                            value = "123456789012",
                        ),
                    ),
                    rawValue = "123456789012",
                    action = null,
                ),
            )
        }

        // Then
        onNodeWithText(getString(Res.string.upc_a)).assertExists()
        onNodeWithText("123456789012").assertExists()
    }

    @Test
    fun `a saved network is told on the sheet without an action`() = runComposeUiTest {
        // When
        setContent {
            val snackbarState = rememberSCSnackbarState()
            LaunchedEffect(Unit) {
                snackbarState.showWifiJoinResult(
                    wifi = WIFI_NETWORK,
                    result = WifiJoinResult.SAVED,
                )
            }
            scanResultSheet(
                result = WIFI,
                snackbarState = snackbarState,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.wifi_saved, "ScannyNet")).assertExists()
        onNode(hasText(getString(Res.string.wifi)) and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun `a declined screen of the system or the wi-fi settings opened tell nothing`() = runComposeUiTest {
        // Given
        var answers: List<Boolean>? = null

        // When
        setContent {
            val snackbarState = rememberSCSnackbarState()
            LaunchedEffect(Unit) {
                // A message shown would hold its answer until it leaves
                answers = listOf(WifiJoinResult.CANCELED, WifiJoinResult.SETTINGS_OPENED).map { result ->
                    snackbarState.showWifiJoinResult(
                        wifi = WIFI_NETWORK,
                        result = result,
                    )
                }
            }
            scanResultSheet(
                result = WIFI,
                snackbarState = snackbarState,
            )
        }

        // Then
        waitUntil { answers != null }
        assertEquals(
            expected = listOf(false, false),
            actual = answers,
        )
    }

    @Composable
    private fun scanResultSheet(
        result: ScanResultItem,
        onActionClicked: (CodeAction) -> Unit = {},
        onCopyClicked: () -> Unit = {},
        onSeeDetailClicked: () -> Unit = {},
        snackbarState: SCSnackbarState = rememberSCSnackbarState(),
    ) {
        SCTheme {
            ScanResultSheet(
                result = result,
                onActionClicked = onActionClicked,
                onCopyClicked = onCopyClicked,
                onSeeDetailClicked = onSeeDetailClicked,
                onDismissRequest = {},
                snackbarState = snackbarState,
            )
        }
    }

    private companion object {
        val SMS = ScanResultItem(
            codeId = 1,
            type = CodeType.SMS,
            format = BarcodeFormat.QR_CODE,
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.PHONE_NUMBER,
                    value = "+33600000000",
                ),
                CodeField(
                    label = CodeFieldLabel.MESSAGE,
                    value = "See you at 10:30 & bring été",
                ),
            ),
            rawValue = "SMSTO:+33600000000:See you at 10:30 & bring été",
            action = CodeAction.WriteSms(link = "sms:+33600000000"),
        )

        val PHONE = ScanResultItem(
            codeId = 4,
            type = CodeType.PHONE,
            format = BarcodeFormat.QR_CODE,
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.PHONE_NUMBER,
                    value = "+33600000000",
                ),
            ),
            rawValue = "tel:+33600000000",
            action = CodeAction.Call(link = "tel:+33600000000"),
        )

        val WIFI_NETWORK = BarcodeContent.Wifi(
            ssid = "ScannyNet",
            encryption = WifiEncryption.WPA,
            password = "secret123",
        )

        val WIFI = ScanResultItem(
            codeId = 3,
            type = CodeType.WIFI,
            format = BarcodeFormat.QR_CODE,
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.NETWORK,
                    value = "ScannyNet",
                ),
            ),
            rawValue = "WIFI:T:WPA;S:ScannyNet;P:secret123;;",
            action = CodeAction.JoinWifi(wifi = WIFI_NETWORK),
        )
    }
}
