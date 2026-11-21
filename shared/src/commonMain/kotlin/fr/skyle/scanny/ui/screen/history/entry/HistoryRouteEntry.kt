package fr.skyle.scanny.ui.screen.history.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.history.ui.HistoryRoute

fun EntryProviderScope<Route>.historyRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.History> {
        HistoryRoute(
            navigateToScanDetail = { codeId ->
                backStack.add(Route.ScanDetail(codeId = codeId))
            },
            navigateToScanGroup = { scanId ->
                backStack.add(Route.ScanGroup(scanId = scanId))
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}