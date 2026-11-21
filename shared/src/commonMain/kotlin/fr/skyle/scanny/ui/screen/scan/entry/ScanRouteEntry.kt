package fr.skyle.scanny.ui.screen.scan.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.metadata
import fr.skyle.scanny.ui.main.navigation.CameraScreenKey
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.scan.ui.ScanRoute

fun EntryProviderScope<Route>.scanRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Scan>(
        metadata = metadata {
            put(CameraScreenKey, true)
        },
    ) {
        ScanRoute(
            navigateToScanDetail = { codeId ->
                backStack.add(Route.ScanDetail(codeId = codeId))
            },
            navigateToScanGroup = { scanId ->
                backStack.add(Route.ScanGroup(scanId = scanId))
            },
            navigateToHistory = {
                backStack.add(Route.History)
            },
            navigateToCreateCode = {
                backStack.add(Route.CreateCode)
            },
            navigateToSettings = {
                backStack.add(Route.Settings)
            },
        )
    }
}