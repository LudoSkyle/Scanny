package fr.skyle.scanny.ui.screen.settings.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.settings.ui.SettingsRoute

fun EntryProviderScope<Route>.settingsRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Settings> {
        SettingsRoute(
            navigateToAbout = {
                backStack.add(Route.About)
            },
            navigateToFeedback = {
                backStack.add(Route.Feedback)
            },
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}