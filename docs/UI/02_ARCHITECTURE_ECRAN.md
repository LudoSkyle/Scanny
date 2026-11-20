## Architecture d'un écran

Un écran se compose, au minimum, d'une `RouteEntry`, d'une `Route` et d'un `Screen`, et si besoin
d'un `ViewModel`. Tout est dans `shared/src/commonMain`.

```
fr/skyle/scanny/
└── ui/
    └── screen/
        └── <feature>/
            ├── entry/
            │   └── <Feature>RouteEntry.kt
            └── ui/
                ├── <Feature>Route.kt
                ├── <Feature>Screen.kt
                ├── <Feature>ViewModel.kt
                ├── component/
                ├── mapper/
                └── model/
```

- `ui/component/` : les composants propres à la feature.
- `ui/model/` : les modèles d'affichage de la feature, quand un modèle métier ne suffit pas tel quel.
- `ui/mapper/` : les conversions modèle métier → modèle d'affichage.
- Un composant « produit » utilisé par plusieurs features vit dans `fr/skyle/scanny/ui/core/`. Un
  composant générique, sans notion de Scanny, vit dans `designSystem`.

### `RouteEntry`

Déclare l'entrée de navigation et branche les callbacks de navigation (voir
[01_NAVIGATION.md](./01_NAVIGATION.md)). Elle ne contient aucune logique.

### `Route`

Une `...Route` :

- récupère son ViewModel (`koinViewModel()`) et collecte son état
  (`collectAsStateWithLifecycle()`) ;
- collecte les events one-shot et déclenche la navigation, les dialogs, les actions de Compose
  (`LocalUriHandler`, `LocalClipboard`, `LocalHapticFeedback`) ;
- branche les callbacks du `Screen` sur le ViewModel.

Elle n'a pas de `Preview` : son ViewModel ne s'y construit pas.

`ui/ScanDetailRoute.kt`

```kotlin
@Composable
fun ScanDetailRoute(
    scanId: Long,
    navigateBack: () -> Unit,
    viewModel: ScanDetailViewModel = koinViewModel(parameters = { parametersOf(scanId) }),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isDeleteErrorDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    ScanDetailViewModel.ScanDetailEvent.ScanDeleted -> {
                        navigateBack()
                    }

                    ScanDetailViewModel.ScanDetailEvent.DeleteFailed -> {
                        isDeleteErrorDialogVisible = true
                    }
                }
            }
        }
    }

    ScanDetailScreen(
        scan = uiState.scan,
        isDeleting = uiState.isDeleting,
        onDeleteClicked = viewModel::deleteScan,
        onBackClicked = navigateBack,
    )

    if (isDeleteErrorDialogVisible) {
        // SCDialog explaining that the scan could not be deleted
    }
}
```

### `Screen`

Un `...Screen` :

- reçoit les valeurs nécessaires à son affichage, et des callbacks explicites ;
- ne contient aucun autre `@Composable` que lui et sa `Preview` : ses sous-composants vivent dans
  `ui/component/` ;
- n'expose pas de paramètre `modifier` : il occupe tout l'écran.

`ui/ScanDetailScreen.kt`

```kotlin
@Composable
fun ScanDetailScreen(
    scan: ScanDetailItem?,
    isDeleting: Boolean,
    onDeleteClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    // Implementation of the screen, made of components from ui/component/
}

@AppScreenPreview
@Composable
private fun PreviewScanDetailScreen() {
    SCTheme {
        ScanDetailScreen(
            scan = ScanDetailItem(
                title = "https://scanny.app",
                format = "QR Code",
            ),
            isDeleting = false,
            onDeleteClicked = {},
            onBackClicked = {},
        )
    }
}
```

### `ViewModel`

Un `...ViewModel` :

- vit en `commonMain` et ne reçoit **jamais** d'objet de plateforme (`Context`, `LifecycleOwner`,
  `UIViewController`) : ce qui en a besoin est derrière une interface native (voir
  [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md)) ;
- reçoit ses dépendances par constructeur : des interfaces de UseCase et d'actions natives, jamais
  une implémentation ;
- expose un seul `StateFlow<XxxUIState>`, couple d'un `MutableStateFlow` privé et de sa vue en
  lecture seule ;
- collecte dans `init` les flows dont son état dépend, pour mettre à jour l'état privé, plutôt que
  d'exposer un `stateIn` ;
- ne fait pas de `viewModelScope.launch` pour une simple mise à jour d'état : `update` est
  synchrone et thread-safe ;
- ne manipule pas de modèle de `data` (il ne le voit pas) ; il convertit un modèle métier en modèle
  d'affichage quand l'écran n'a besoin que d'une partie des champs ou d'une forme préparée ;
- est déclaré dans `di/SharedModule.kt` avec `viewModelOf(::XxxViewModel)`.

`ui/ScanDetailViewModel.kt`

```kotlin
class ScanDetailViewModel(
    private val scanId: Long,
    private val watchScanUseCase: WatchScanUseCase,
    private val deleteScanUseCase: DeleteScanUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanDetailUIState())
    val uiState: StateFlow<ScanDetailUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchScanUseCase(scanId = scanId).collect { scan ->
                _uiState.update { uiState ->
                    uiState.copy(scan = scan?.toScanDetailItem())
                }
            }
        }
    }

    fun deleteScan() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isDeleting = true)
            }

            try {
                deleteScanUseCase(scanId = scanId)

                _event.send(ScanDetailEvent.ScanDeleted)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to delete scan $scanId" }

                _event.send(ScanDetailEvent.DeleteFailed)
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isDeleting = false)
                }
            }
        }
    }

    data class ScanDetailUIState(
        val scan: ScanDetailItem? = null,
        val isDeleting: Boolean = false,
    )

    sealed interface ScanDetailEvent {
        data object ScanDeleted : ScanDetailEvent
        data object DeleteFailed : ScanDetailEvent
    }
}
```

### Events one-shot

Les évènements à usage unique (navigation, dialog d'erreur, ouverture d'un lien, vibration) ne
transitent pas par l'`UIState`, mais par un `Channel` exposé en `Flow` :

- le ViewModel déclare `private val _event = Channel<XxxEvent>(Channel.BUFFERED)` et
  `val event = _event.receiveAsFlow()` ;
- il émet avec `_event.send(...)`, depuis une coroutine ;
- la `Route` collecte dans un `LaunchedEffect(Unit)` englobé par
  `lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)`, pour ne traiter les events que quand
  l'écran est visible.

### Règles importantes

- Une `Route` n'a pas de `Preview`. Tous les autres composables d'un écran en ont au moins une.
- Un état que l'utilisateur ouvre ou saisit (dialog, bottom sheet, texte en cours) se garde avec
  `rememberSaveable` ; chaque dialog a son propre booléen de visibilité.
- Une bottom sheet qui charge des données a son propre ViewModel et ne reçoit que des identifiants,
  jamais l'objet chargé par l'écran qui l'ouvre.