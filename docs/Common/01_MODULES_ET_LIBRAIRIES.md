## Modules et bibliothèques

### Modules

| Module | Type | Contenu |
|---|---|---|
| `androidApp` | App Android | `ScannyApplication` (démarrage de Koin, logs, Crashlytics), `MainActivity` qui affiche `ScannyApp()`, manifest, icône, signature, `google-services.json` |
| `iosApp` | Projet Xcode | L'app SwiftUI qui démarre Koin et affiche le `ComposeUIViewController` de `shared`, `Info.plist`, assets, signature |
| `shared` | KMP + Compose Multiplatform | `ScannyApp`, navigation, écrans (`ui/screen/<feature>`), ViewModels, composants « produit » (`ui/core`), code natif des écrans (`platform/`), module Koin racine. Produit le framework iOS `Shared` |
| `designSystem` | KMP + Compose Multiplatform | `SCTheme` (couleurs, typographie, formes), composants génériques (`SCScreenStructure`, `SCTopAppBar`, `SCIconButton`…) qui enveloppent Material 3, polices |
| `domain` | KMP | Modèles métier (`BarcodeContent`, `BarcodeFormat`…), parseur et encodeur du contenu des codes, UseCases |
| `data` | KMP | Base Room (entités, DAO), préférences DataStore, Repositories (interfaces et implémentations), modèles exposés |

Le nom de package suit le module : `fr.skyle.scanny` pour `shared` et les hôtes,
`fr.skyle.scanny.designsystem`, `fr.skyle.scanny.domain`, `fr.skyle.scanny.data`.

### Graphe de dépendances

```
androidApp ──► shared ◄── iosApp (framework Shared)
                 │
         ┌───────┴────────┐
         ▼                ▼
   designSystem         domain
                          │
                          ▼
                        data
```

- Toutes les dépendances entre modules sont en `implementation`. `shared` ne voit donc pas `data` :
  il passe par les UseCases de `domain` (voir
  [ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)).
- `designSystem` ne dépend que de Compose et de Material 3 : le compilateur garantit qu'un composant
  générique ne connaît ni un écran, ni un modèle métier.
- `domain` et `data` n'ont aucune dépendance UI.
- Les hôtes ne dépendent que de `shared`. Ils ne contiennent aucun écran.

### Visibilité entre modules

- `internal` par défaut. Est public uniquement ce qu'un autre module utilise.
- `data` garde `internal` ses entités, DAO, base, converters, mappers et implémentations de
  Repository. Sont publics : les interfaces de Repository, les modèles qu'elles exposent, le module
  Koin.
- `domain` garde `internal` ses implémentations de UseCase. Sont publics : les interfaces de
  UseCase, les modèles métier, le parseur et l'encodeur s'ils servent à l'UI, le module Koin.
- `designSystem` expose ses composants, leurs `Defaults` et `SCTheme`.

### Injection de dépendances

Koin, en DSL (`module { }`, `singleOf`, `factoryOf`, `viewModelOf`).

- Chaque module déclare son module Koin dans un fichier unique `di/<Module>Module.kt`
  (`dataModule`, `domainModule`, `sharedModule`). Les classes reçoivent leurs dépendances par
  constructeur, sans annotation : **dans `data` et `domain`, seul le package `di/` importe Koin**.
- Chaque module Koin inclut celui du module dont il dépend (`includes(...)`) : les hôtes ne
  démarrent que `sharedModule`.
- Ce qui diffère par plateforme (construction de la base, chemin des préférences, implémentations
  natives) est déclaré dans un module Koin de plateforme, `expect`/`actual`, inclus par le module
  commun (voir [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md)).
- Android démarre Koin dans `ScannyApplication` avec `androidContext(...)` ; iOS appelle `initKoin()`
  (dans `shared/iosMain`) au démarrage de l'app SwiftUI.

### Bibliothèques

Les versions sont dans `gradle/libs.versions.toml`, jamais dans la documentation.

Communes (`commonMain`) :

| Besoin | Bibliothèque |
|---|---|
| UI | Compose Multiplatform (`ui`, `foundation`, `components-resources`, `ui-tooling-preview`) |
| Composants Material | Material 3, en alpha alignée sur Compose Multiplatform, **dans `designSystem` seulement**, sans API Expressive |
| Textes, polices, icônes | Compose Resources |
| Navigation | Navigation 3 (artefacts multiplateformes) |
| ViewModel, cycle de vie | Lifecycle multiplateforme (`lifecycle-viewmodel`, `-viewmodel-compose`, `-runtime-compose`, `-viewmodel-navigation3`) |
| Injection de dépendances | Koin (`koin-core`, `koin-compose-viewmodel`) |
| Base de données | Room KMP, avec `sqlite-bundled` et KSP |
| Préférences | DataStore Preferences (multiplateforme), sur okio |
| Sérialisation | kotlinx-serialization (routes de navigation) |
| Dates et heure | kotlinx-datetime, `kotlin.time.Clock` |
| Fichiers | kotlinx-io |
| Coroutines | kotlinx-coroutines |
| Logs | Kermit (`Logger`). Sur Android, `ScannyApplication` envoie avertissements et erreurs à Crashlytics (`CrashlyticsLogWriter`), et le build debug ajoute logcat |
| Génération de QR codes | qrose (rendu `Painter`, export PNG, personnalisation) et qrose-oned (les codes-barres d'une dimension), dans `shared` : un code se redessine depuis son texte par `CodeImage` (`ui/core/code`), et son image PNG, à partager ou enregistrer, par `CodeImageRenderer`, tous deux dans le style d'un QR code créé (`CodeStyleMapper`) |

Android (`androidMain`, `androidApp`) :

| Besoin | Bibliothèque |
|---|---|
| Caméra | CameraX (`camera-camera2`, `camera-lifecycle`, `camera-compose`) |
| Détection | ML Kit Barcode Scanning |
| Activité | `activity-compose`, `core-splashscreen` |
| Remontée de crashs | Firebase Crashlytics |
| Ruban « DEBUG » sur l'icône | easylauncher |

iOS (`iosMain`) : les frameworks système, par l'interop Kotlin/Native — AVFoundation (caméra et
détection en direct), Vision (détection dans une image), PhotosUI, ContactsUI, NetworkExtension,
StoreKit, UIKit.

### Ce qu'on n'utilise pas

- **Hilt** : n'existe que sur Android.
- **ZXing**, ou toute bibliothèque de scan tierce : choix produit, la détection reste native. ZXing
  n'entre que dans les tests JVM de `shared`, pour relire les codes que l'app dessine.
- **Timber** dans le code commun : n'existe que sur Android. Les logs passent par Kermit.
- **`material-icons-extended`** : n'est plus maintenu pour Compose Multiplatform. Les icônes sont des
  Material Symbols en vectoriel, dans Compose Resources.
- **Client réseau** : Scanny n'appelle aucune API. Si le besoin apparaît, Ktor.