package fr.skyle.scanny.ui.screen.about.ui

import fr.skyle.scanny.fake.FakeAppVersion
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutViewModelTest {

    @Test
    fun `the version of the app is shown with its build`() {
        // When
        val viewModel = AboutViewModel(
            appVersion = FakeAppVersion(
                name = "2.1",
                build = "21",
            ),
        )

        // Then
        assertEquals(
            expected = AboutViewModel.AboutUIState(
                versionName = "2.1",
                build = "21",
            ),
            actual = viewModel.uiState.value,
        )
    }
}