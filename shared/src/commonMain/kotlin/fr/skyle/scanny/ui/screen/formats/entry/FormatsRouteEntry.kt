package fr.skyle.scanny.ui.screen.formats.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.formats.ui.FormatsRoute

fun EntryProviderScope<Route>.formatsRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Formats> {
        FormatsRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
