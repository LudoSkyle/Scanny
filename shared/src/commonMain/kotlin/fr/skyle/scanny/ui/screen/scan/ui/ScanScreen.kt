package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppScreenPreview

@Composable
fun ScanScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SCTheme.colors.backgroundBlack),
    )
}

@AppScreenPreview
@Composable
private fun PreviewScanScreen() {
    SCTheme {
        ScanScreen()
    }
}