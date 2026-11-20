package fr.skyle.scanny.ui.screen.scan.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.scan.ui.ScanRoute

fun EntryProviderScope<Route>.scanRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Scan> {
        ScanRoute()
    }
}