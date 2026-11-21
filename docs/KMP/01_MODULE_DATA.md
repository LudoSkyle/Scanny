## Module `data`

Le module `data` stocke ce que Scanny garde d'un lancement à l'autre : l'historique des scans,
l'historique des créations et les réglages. Il n'y a pas d'appel réseau.

Il ne connaît pas `domain` (voir [ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)) : il
stocke et rend des types simples (`String`, `Long`, `Instant`), et c'est `domain` qui les
interprète. Le format d'un code, par exemple, est stocké par son nom ; `domain` le convertit en
`BarcodeFormat`.

```
data/src/
├── commonMain/kotlin/fr/skyle/scanny/data/
│   ├── di/
│   │   └── DataModule.kt          (dataModule, expect platformDataModule)
│   ├── db/
│   │   ├── ScannyDatabase.kt      (base, constructeur généré, configuration commune)
│   │   ├── dao/
│   │   ├── entity/
│   │   └── relation/              (une entité et celles qui lui sont liées, lues ensemble)
│   ├── preferences/               (création du DataStore, nom du fichier)
│   ├── mapper/                    (entité -> modèle exposé)
│   ├── model/                     (modèles exposés par les Repositories)
│   └── repository/
│       ├── scan/
│       │   ├── api/
│       │   │   └── ScanRepository.kt
│       │   └── ScanRepositoryImpl.kt
│       └── settings/              (SettingsRepository, sur le DataStore)
├── androidMain/kotlin/fr/skyle/scanny/data/
│   ├── db/ScannyDatabase.android.kt   (construction avec le Context)
│   └── di/DataModule.android.kt       (base et DataStore de la plateforme)
└── iosMain/kotlin/fr/skyle/scanny/data/
    ├── db/ScannyDatabase.ios.kt
    ├── di/DataModule.ios.kt
    └── file/ApplicationSupport.kt     (chemin d'un fichier dans Application Support)
```

### Repositories

- **Chaque Repository a son interface (`api/`) et son implémentation**, dans le même package de
  feature.
- L'interface est publique (`domain` l'utilise), l'implémentation est `internal`.
- **Un modèle exposé et son entité sont deux types distincts**, reliés par un mapper : les
  annotations Room restent dans le module.
- **Un Repository rend ses lectures hors du thread principal.** Room exécute la requête sur son
  propre contexte, mais ce qui la suit tourne dans celui de l'appelant. Chaque observation se
  termine donc par `flowOn(Dispatchers.Default)`, et une lecture `suspend` qui convertit son
  résultat le fait dans `withContext(Dispatchers.Default)`.
- Pas de `runBlocking`, jamais : toute lecture est `suspend` ou un `Flow`.

`repository/scan/api/ScanRepository.kt`

```kotlin
interface ScanRepository {

    /** Records a capture of [codes], at least one, in their order, and returns its id. */
    suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
    ): Long

    fun watchScans(): Flow<List<StoredScan>>

    fun watchScan(id: Long): Flow<StoredScan?>

    fun watchCode(id: Long): Flow<StoredCode?>

    suspend fun deleteScan(id: Long)

    /** Deletes a code, and its capture when it was the last code of it. */
    suspend fun deleteCode(id: Long)
}
```

`repository/scan/ScanRepositoryImpl.kt` (extrait)

```kotlin
internal class ScanRepositoryImpl(
    private val scanDao: ScanDao,
) : ScanRepository {

    override suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
    ): Long {
        require(codes.isNotEmpty()) { "A scan holds at least one code" }

        return scanDao.insertWithCodes(
            scan = ScanEntity(scannedAt = scannedAt.toEpochMilliseconds()),
            codes = codes.mapIndexed { index, code ->
                ScannedCodeEntity(
                    // Set by the DAO, once the scan is inserted
                    scanId = 0,
                    position = index,
                    rawValue = code.rawValue,
                    format = code.format,
                )
            },
        )
    }

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scanDao.watchById(id = id)
            .map { scan ->
                scan?.toStoredScan()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    override suspend fun deleteCode(id: Long) {
        scanDao.deleteCodeAndEmptyScan(id = id)
    }
}
```

`di/DataModule.kt`

```kotlin
val dataModule = module {
    includes(platformDataModule)

    single { get<ScannyDatabase>().scanDao() }

    singleOf(::ScanRepositoryImpl) bind ScanRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
}

internal expect val platformDataModule: Module
```

### Base de données (Room)

- Une seule base, `ScannyDatabase`, `internal`. Elle porte `@ConstructedBy` : Room génère son
  constructeur pour chaque plateforme, sans réflexion.
- Elle est construite par le module Koin de plateforme, avec `BundledSQLiteDriver` (le SQLite
  embarqué, le même partout) et `setQueryCoroutineContext(Dispatchers.IO)`. Le constructeur généré
  est une classe `expect` : le module compile avec `-Xexpect-actual-classes`.
- Son fichier est sauvegardé par le système, puisque l'historique est une donnée que l'utilisateur
  veut retrouver sur un nouveau téléphone : dans le dossier `databases` de l'app sur Android
  (`getDatabasePath`), dans Application Support sur iOS.
- Les DAO ne déclarent que des fonctions `suspend` ou des `Flow` : aucun appel bloquant.
- **Les historiques sont des données de l'utilisateur : jamais de migration destructive.** Toute
  évolution du schéma monte la version et fournit sa migration (`AutoMigration` quand c'est
  possible). Les schémas sont exportés et versionnés (`data/schemas/`).
- La règle vaut avant même la publication de la v2 : les installations de développement ont des
  données, et une base dont le schéma change sans que sa version monte ne s'ouvre plus. La version 2
  ajoute `is_in_history` à `scan` (`AutoMigration` de 1 à 2), la version 3 la table `creation` (de 2
  à 3), la version 4 les couleurs et les formes d'une création (de 3 à 4 : une création plus
  ancienne garde ce qu'elle était, noir sur blanc et carrée), la version 5 son logo (de 4 à 5, sans
  logo par défaut).
- Une requête de lecture multiple a un `ORDER BY` explicite : sans lui, l'ordre est un détail de
  SQLite dont l'UI finit par dépendre.
- Un index est attendu sur toute colonne utilisée en filtre ou en tri (date de scan, type).
- `@Insert(onConflict = REPLACE)` supprime puis réinsère la ligne : `@Upsert` dès qu'une ligne est
  réenregistrée (favori, renommage).
- Une requête qui retourne une relation (`@Relation`) porte `@Transaction`.
- Plusieurs requêtes qui doivent réussir ou échouer ensemble vont dans une méthode du DAO annotée
  `@Transaction`. Une telle méthode a un corps : le DAO est alors une `abstract class` (méthodes
  `abstract` pour les requêtes, `open` pour les transactions), Room ne sachant pas exécuter le corps
  d'une méthode d'interface dans une transaction.

### Historique des scans

Un scan est une **capture** de un à plusieurs codes : un code pour un scan simple, tous les codes
trouvés pendant un scan multiple. Deux tables :

| Table | Colonnes | Rôle |
|---|---|---|
| `scan` | `id`, `scanned_at` (indexée), `is_in_history` (indexée, `1` par défaut) | la capture, sa date, et si elle est dans l'historique |
| `scanned_code` | `id`, `scan_id` (indexée, clé étrangère vers `scan`, `ON DELETE CASCADE`), `position`, `raw_value`, `format` | un code de la capture, à sa place dans l'ordre de lecture |

- Une capture et ses codes s'enregistrent dans une même transaction (`insertWithCodes`) : une
  capture n'existe jamais sans ses codes.
- **Une capture a toujours au moins un code** : `saveScan` refuse une liste vide, et la suppression
  du dernier code d'une capture supprime la capture dans la même transaction
  (`deleteCodeAndEmptyScan`). Supprimer une capture supprime ses codes, par la cascade ; effacer
  l'historique (`deleteAll`) supprime toutes les captures, et leurs codes de la même façon.
- Les captures se lisent de la plus récente à la plus ancienne ; l'`id` départage deux captures de la
  même milliseconde. Les codes d'une capture se rendent dans l'ordre de `position`.
- Un code se lit seul (`watchCode`) avec la date de sa capture : l'écran d'un code ne charge pas
  tout le groupe.
- Une capture hors de l'historique (`is_in_history = 0`, quand l'utilisateur n'en garde pas) se lit
  par son identifiant, pour la feuille du résultat et le détail, mais jamais dans l'historique ni
  comme sa dernière capture (`findLatestScan`). La purge (`purgeScans`) la supprime, avec les
  captures de l'historique plus anciennes que la date donnée.
- `updateScanTime` remet une capture à l'heure, pour un doublon consécutif (voir le domaine).

### Historique des créations

| Table | Colonnes | Rôle |
|---|---|---|
| `creation` | `id`, `created_at` (indexée), `raw_value`, `format`, `dark_color`, `light_color` (ARGB, noir et blanc par défaut), `dot_shape`, `corner_shape` (`SQUARE` par défaut), `logo` (un PNG de 256 pixels de côté au plus, nul par défaut), `is_in_history` (indexée, `1` par défaut) | un code créé : le texte mis dans le code, son format, la façon de le dessiner, sa date |

- Seul le texte du code est stocké, comme pour un scan : le contenu se relit depuis lui.
- Comme une capture, une création hors de l'historique (`is_in_history = 0`) se lit par son
  identifiant mais jamais dans l'historique, et la purge (`purgeCreations`) la supprime avec les
  créations plus anciennes que la date donnée.

### Réglages (DataStore)

- Un seul DataStore de préférences pour toute l'app, créé une fois (`single` Koin) avec
  `PreferenceDataStoreFactory.createWithPath`. Le chemin du fichier vient du module de plateforme
  (Application Support sur iOS).
- Un fichier de réglages corrompu repart des valeurs par défaut (`ReplaceFileCorruptionHandler`) :
  c'est un incident de l'appareil, pas un bug, et chaque lecture échouerait sinon. C'est la seule
  observation de `data` qui traite une erreur.
- Sur Android, le fichier et les clés de la v1 sont conservés pour que les réglages survivent à la
  mise à jour : fichier `sc_datastore.preferences_pb` dans le dossier `datastore` des fichiers de
  l'app, clés `KEY_IS_VIBRATION_AFTER_SCAN_ENABLED` (défaut `true`), `KEY_OPEN_LINK_AFTER_SCAN`
  (défaut `false` ; le réglage ouvre désormais tout contenu, la clé garde son nom de la v1).
  `KEY_IS_RAW_CONTENT_SHOWN` n'est plus lue : le contenu brut est toujours dans le détail, replié ;
  la clé reste dans le fichier d'un utilisateur de la v1, sans effet. Les réglages venus avec la v2
  suivent la même forme : `KEY_IS_BEEP_AFTER_SCAN_ENABLED` et `KEY_IS_COPY_AFTER_SCAN_ENABLED`
  (défaut `false`), `KEY_IS_FRONT_CAMERA_USED` (défaut `false`), `KEY_IS_SCAN_HISTORY_ENABLED`,
  `KEY_IS_CREATION_HISTORY_ENABLED` et `KEY_IS_CONSECUTIVE_DUPLICATE_SKIPPED` (défaut
  `true`), et `KEY_HISTORY_RETENTION_DAYS`, un entier (défaut `30`, `0` pour toujours) : `data` ne
  connaît pas l'énumération du domaine.
- `SettingsRepository` expose un `watchXxx(): Flow<T>` et un `suspend fun setXxx(value: T)` par
  réglage. La valeur par défaut est une constante du Repository, appliquée à la lecture.