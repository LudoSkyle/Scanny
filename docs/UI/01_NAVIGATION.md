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
- Ce que l'app fait une fois à son démarrage, quel que soit l'écran (la purge de l'historique), vit
  dans `ui/main/MainViewModel`, créé par `ScannyApp` : pas dans le ViewModel du scan.
- La création d'un code s'ouvre par le « + » de la barre du scan (`Route.CreateCode()`), ou par
  « Modifier le code » du détail d'un scan, prérempli avec son contenu
  (`Route.CreateCode(scannedCodeId)`), comme du détail d'une création, avec son contenu et son style
  (`Route.CreateCode(creationId)`) : dans les deux cas, créer fait un nouveau code. L'entrée traduit
  la route en `CreationSource` (`New`, `ScannedCode`, `CreatedCode`), seul paramètre du ViewModel. Créer remplace l'éditeur par l'écran de la création
  (`Route.CreationDetail(creationId)`) : le retour depuis elle revient à l'écran qui a ouvert
  l'éditeur.
- L'éditeur s'ouvre sur le choix de la famille du code : QR code, code 2D (Data Matrix, Aztec ou
  PDF417, qui tiennent les mêmes contenus qu'un QR code) ou code-barres (une valeur selon son
  format). Celui d'un QR code a deux étapes sur la même route : le contenu et son aperçu, puis la
  personnalisation (le titre de l'étape, « Étape 1 sur 2 » en sous-titre ; un code 2D et un
  code-barres, sombres sur clair, n'ont que la première). Une barre fixée sous les deux étapes, au-dessus
  du clavier, porte le code en miniature (touchée, elle l'ouvre en grand dans une feuille), la taille
  que le contenu prend du QR code ou ce qui empêche le code, et le bouton Suivant ou Créer ; elle
  reste pendant que les étapes glissent. La personnalisation garde le code en grand en haut : elle se
  juge sur ses couleurs et son logo. Ses champs vont par cartes titrées, les plus rares repliés (voir
  [KMP/02_MODULE_DOMAIN.md](../KMP/02_MODULE_DOMAIN.md)). L'étape vit dans le ViewModel : revenir au contenu garde la personnalisation, quitter
  l'éditeur la perd avec lui. Le retour de la personnalisation, de la barre comme du système (bouton,
  geste), ramène au contenu : un `NavigationBackHandler` (navigationevent) l'intercepte tant qu'elle
  est montrée, sans toucher au back stack.
- L'historique (`Route.History`) montre les scans ou les créations, sur le même écran : une bascule
  sous sa barre (`SCSegmentedControl`), ou un glissement entre les deux vues (`HorizontalPager`),
  choisit la vue, sans toucher au back stack. La page posée et la bascule se suivent : le ViewModel
  garde la vue choisie, la page s'y place, et une page posée par le doigt la choisit. Ce n'est pas
  une navigation par onglets : chaque vue ouvre ses écrans par-dessus l'historique, qui garde la vue
  choisie au retour. Depuis la première vue, le geste retour depuis le bord quitte l'historique.
  Effacer ne vide que la vue montrée.
- Les réglages ouvrent le retour utilisateur (`Route.Feedback`) et, depuis leur section FAQ, les
  formats de codes (`Route.Formats`) : tous les formats que Scanny lit, chacun replié sous son nom
  avec son usage, comment il est formé et un exemple décortiqué, les textes de la fiche du format d'un
  détail. Un écran sans logique, comme celui-ci, n'a pas de ViewModel.
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

Ces transitions sont simples, les mêmes sur les deux plateformes (`NavTransitions.kt`) : l'écran
ouvert glisse depuis la fin et pousse celui qu'il quitte, tous deux sur toute la largeur, en un
`tween()` par défaut ; le retour fait l'inverse, et le geste retour joue la même transition que le
retour. Les transitions par défaut de Navigation 3 sur Android, un fondu enchaîné, et au geste retour
l'écran quitté qui rétrécit, sont écartées.

Vers et depuis l'écran de la caméra, l'autre écran apparaît et disparaît en fondu par-dessus elle,
qui ne bouge pas : un aperçu de caméra déplacé ou estompé par Compose se rend mal. L'écran de la
caméra le déclare dans ses `metadata` (`CameraScreenKey`) ; ce sont les transitions globales qui le
lisent, sur l'écran de départ comme sur l'écran d'arrivée, car Navigation 3 ne consulte que les
`metadata` de l'écran du dessus, et l'écran de la caméra est toujours en bas de la pile.