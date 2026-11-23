package fr.skyle.scanny.ui.screen.scandetail.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.scandetail.ui.ScanDetailRoute

fun EntryProviderScope<Route>.scanDetailRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ScanDetail> { route ->
        ScanDetailRoute(
            codeId = route.codeId,
            navigateToCreateCode = {
                backStack.add(Route.CreateCode(scannedCodeId = route.codeId))
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}