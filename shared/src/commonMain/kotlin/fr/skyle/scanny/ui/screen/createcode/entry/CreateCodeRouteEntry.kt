package fr.skyle.scanny.ui.screen.createcode.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.createcode.ui.CreateCodeRoute

fun EntryProviderScope<Route>.createCodeRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.CreateCode> { route ->
        CreateCodeRoute(
            scannedCodeId = route.scannedCodeId,
            // The editor gives way to the creation: back from it returns to the screen that opened it
            navigateToCreationDetail = { creationId ->
                backStack.removeLastOrNull()
                backStack.add(Route.CreationDetail(creationId = creationId))
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}