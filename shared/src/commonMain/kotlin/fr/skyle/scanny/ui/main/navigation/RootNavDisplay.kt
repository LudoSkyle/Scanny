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
import fr.skyle.scanny.ui.screen.scan.entry.scanRouteEntry

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
        },
    )
}