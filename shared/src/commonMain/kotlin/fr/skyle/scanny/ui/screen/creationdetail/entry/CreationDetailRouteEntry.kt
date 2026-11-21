package fr.skyle.scanny.ui.screen.creationdetail.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.creationdetail.ui.CreationDetailRoute

fun EntryProviderScope<Route>.creationDetailRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.CreationDetail> { route ->
        CreationDetailRoute(
            creationId = route.creationId,
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}