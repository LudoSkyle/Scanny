package fr.skyle.scanny.ui.screen.scanhistory.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.scanhistory.ui.ScanHistoryRoute

fun EntryProviderScope<Route>.scanHistoryRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ScanHistory> {
        ScanHistoryRoute(
            navigateToScanDetail = { codeId ->
                backStack.add(Route.ScanDetail(codeId = codeId))
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}