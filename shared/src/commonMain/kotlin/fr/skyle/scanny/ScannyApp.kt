package fr.skyle.scanny

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.main.navigation.RootNavDisplay

@Composable
fun ScannyApp() {
    SCTheme {
        RootNavDisplay(modifier = Modifier.fillMaxSize())
    }
}