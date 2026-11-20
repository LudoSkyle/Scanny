package fr.skyle.scanny.ui.screen.feedback.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.scanny.ui.main.navigation.Route
import fr.skyle.scanny.ui.screen.feedback.ui.FeedbackRoute

fun EntryProviderScope<Route>.feedbackRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Feedback> {
        FeedbackRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}