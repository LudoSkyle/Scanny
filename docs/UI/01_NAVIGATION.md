## Navigation

La navigation utilise Navigation 3, dans ses artefacts multiplateformes : elle est commune à Android
et iOS, dans `shared/src/commonMain`.

**Règle produit, non négociable : pas de barre de navigation en bas.** L'app s'ouvre directement sur
le scan (`Route.Scan`, premier et seul élément du back stack au lancement). Tous les autres écrans
(historiques, création, réglages) s'ouvrent depuis le scan, par une action de sa barre du haut ou de
son interface, et y reviennent par le retour. Ni onglets, ni `NavigationBar`, ni écran d'accueil
intermédiaire.

- Les routes sont centralisées dans `ui/main/navigation/Route.kt`.
- L'hôte de navigation est `ui/main/navigation/RootNavDisplay.kt`, affiché par `ScannyApp`.
- Une route implémente `NavKey` et est annotée `@Serializable` : le back stack est sauvegardé et
  restauré (recréation de l'activité, mort du process sur Android).
- Le back stack se crée avec `rememberSerializable` et un `NavBackStackSerializer` construit sur le
  sérialiseur de `Route` : c'est ce qui le rend sauvegardable sur iOS aussi, où aucune réflexion
  n'est disponible. Jamais `remember { mutableStateListOf(...) }`, qui perd la navigation.
- Une route ne transporte que des identifiants ou des valeurs simples (l'id d'un code scanné, un
  type de QR à créer), jamais un objet chargé : l'écran ouvert recharge ce dont il a besoin.
- Chaque entrée de navigation a son propre `ViewModelStore` (`rememberViewModelStoreNavEntryDecorator`)
  et son propre état sauvegardé (`rememberSaveableStateHolderNavEntryDecorator`) : un ViewModel vit
  aussi longtemps que son entrée dans le back stack.
- Une `...Route` ne reçoit jamais le back stack : elle reçoit des callbacks de navigation
  (`navigateToSettings`, `navigateBack`), branchés dans son `entry`.

Exemples de déclaration :

`ui/main/navigation/Route.kt`

```kotlin
@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Scan : Route

    @Serializable
    data class ScanDetail(val codeId: Long) : Route

    @Serializable
    data object Settings : Route
}
```

`ui/main/navigation/RootNavDisplay.kt`

```kotlin
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

            settingsRouteEntry(backStack = backStack)
        },
    )
}
```

Chaque écran déclare son `entry` dans `ui/screen/<feature>/entry/<Feature>RouteEntry.kt` (voir
[02_ARCHITECTURE_ECRAN.md](./02_ARCHITECTURE_ECRAN.md)) :

```kotlin
fun EntryProviderScope<Route>.scanDetailRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ScanDetail> { route ->
        ScanDetailRoute(
            codeId = route.codeId,
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
```

Les animations de transition sont déclarées une fois, sur `NavDisplay` (`transitionSpec`,
`popTransitionSpec`, `predictivePopTransitionSpec`), pas écran par écran. Une entrée qui doit
s'écarter du comportement par défaut le déclare dans ses `metadata`.

Ces transitions sont celles d'iOS, sur les deux plateformes (`NavTransitions.kt`) : l'écran ouvert
glisse depuis la fin par-dessus celui qu'il quitte, qui recule d'un quart en s'assombrissant ; le
retour fait l'inverse, et le geste retour fait suivre le doigt à l'écran quitté. Les transitions par
défaut de Navigation 3 sur Android, un fondu enchaîné, et au geste retour l'écran quitté qui
rétrécit, sont écartées.