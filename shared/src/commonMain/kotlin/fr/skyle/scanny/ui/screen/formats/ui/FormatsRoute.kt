package fr.skyle.scanny.ui.screen.formats.ui

import androidx.compose.runtime.Composable

// Texts only: no ViewModel
@Composable
fun FormatsRoute(
    navigateBack: () -> Unit,
) {
    FormatsScreen(onBackClicked = navigateBack)
}
