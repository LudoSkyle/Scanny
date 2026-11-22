package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.account
import fr.skyle.scanny.resources.issuer
import fr.skyle.scanny.resources.latitude
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.longitude
import fr.skyle.scanny.resources.place
import fr.skyle.scanny.resources.secret_key
import fr.skyle.scanny.resources.title
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toBarcodeContent
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CreationFormFieldsTest : ComposeTest() {

    @Test
    fun `the fields of a location fill its coordinates with a decimal comma and its place`() = runComposeUiTest {
        // Given
        var form by mutableStateOf(CreationForm(kind = CreationKind.LOCATION))
        setContent {
            SCTheme {
                CreationFormFields(
                    form = form,
                    onFormChanged = { changedForm -> form = changedForm },
                )
            }
        }

        // When
        onNode(hasSetTextAction() and hasText(getString(Res.string.latitude))).performTextInput("48,8584")
        onNode(hasSetTextAction() and hasText(getString(Res.string.longitude))).performTextInput("2.2945")
        onNode(hasSetTextAction() and hasText(getString(Res.string.place))).performTextInput("Tour Eiffel")

        // Then
        assertEquals(
            expected = BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            actual = form.toBarcodeContent(),
        )
    }

    @Test
    fun `the fields of an otp key fill its issuer its account and its secret`() = runComposeUiTest {
        // Given
        var form by mutableStateOf(CreationForm(kind = CreationKind.OTP))
        setContent {
            SCTheme {
                CreationFormFields(
                    form = form,
                    onFormChanged = { changedForm -> form = changedForm },
                )
            }
        }

        // When
        onNode(hasSetTextAction() and hasText(getString(Res.string.issuer))).performTextInput("Scanny")
        onNode(hasSetTextAction() and hasText(getString(Res.string.account))).performTextInput("john@doe.fr")
        onNode(hasSetTextAction() and hasText(getString(Res.string.secret_key))).performTextInput("jbsw y3dp ehpk 3pxp")

        // Then
        assertEquals(
            expected = BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                issuer = "Scanny",
                account = "john@doe.fr",
            ),
            actual = form.toBarcodeContent(),
        )
    }

    @Test
    fun `the fields of a bookmark fill its title and its link`() = runComposeUiTest {
        // Given
        var form by mutableStateOf(CreationForm(kind = CreationKind.BOOKMARK))
        setContent {
            SCTheme {
                CreationFormFields(
                    form = form,
                    onFormChanged = { changedForm -> form = changedForm },
                )
            }
        }

        // When
        onNode(hasSetTextAction() and hasText(getString(Res.string.title))).performTextInput("Scanny")
        onNode(hasSetTextAction() and hasText(getString(Res.string.link))).performTextInput("scanny.app")

        // Then
        assertEquals(
            expected = BarcodeContent.Bookmark(
                url = "https://scanny.app",
                title = "Scanny",
            ),
            actual = form.toBarcodeContent(),
        )
    }
}
