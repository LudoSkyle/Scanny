package fr.skyle.scanny.ui.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import fr.skyle.scanny.ui.screen.feedback.entry.feedbackRouteEntry
import fr.skyle.scanny.ui.screen.scan.entry.scanRouteEntry
import fr.skyle.scanny.ui.screen.scandetail.entry.scanDetailRouteEntry
import fr.skyle.scanny.ui.screen.scangroup.entry.scanGroupRouteEntry
import fr.skyle.scanny.ui.screen.scanhistory.entry.scanHistoryRouteEntry
import fr.skyle.scanny.ui.screen.settings.entry.settingsRouteEntry

@Composable
fun RootNavDisplay(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(Route.serializer())) {
        NavBackStack(Route.Scan)
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            scanRouteEntry(backStack = backStack)

            scanDetailRouteEntry(backStack = backStack)

            scanGroupRouteEntry(backStack = backStack)

            scanHistoryRouteEntry(backStack = backStack)

            settingsRouteEntry(backStack = backStack)

            feedbackRouteEntry(backStack = backStack)
        },
        transitionSpec = forwardTransition(),
        popTransitionSpec = backTransition(),
        predictivePopTransitionSpec = backGestureTransition(),
    )
}