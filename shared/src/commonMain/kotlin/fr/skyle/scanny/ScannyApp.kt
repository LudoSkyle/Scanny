package fr.skyle.scanny

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.main.MainViewModel
import fr.skyle.scanny.ui.main.navigation.RootNavDisplay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScannyApp() {
    // Created with the app, for what it does at its start
    koinViewModel<MainViewModel>()

    SCTheme {
        RootNavDisplay(modifier = Modifier.fillMaxSize())
    }
}