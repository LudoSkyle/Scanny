package fr.skyle.scanny.ui.screen.scangroup.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.scangroup.ui.ScanGroupRoute

fun EntryProviderScope<Route>.scanGroupRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ScanGroup> { route ->
        ScanGroupRoute(
            scanId = route.scanId,
            navigateToScanDetail = { codeId ->
                backStack.add(Route.ScanDetail(codeId = codeId))
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}