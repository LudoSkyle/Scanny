package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppScreenPreview

@Composable
fun ScanScreen() {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BLACK,
    ) {
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanScreen() {
    SCTheme {
        ScanScreen()
    }
}