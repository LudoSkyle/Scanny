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
│   │   └── entity/
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

    suspend fun saveScan(
        rawValue: String,
        format: String,
        scannedAt: Instant,
    ): Long

    fun watchScans(): Flow<List<StoredScan>>

    fun watchScan(id: Long): Flow<StoredScan?>

    suspend fun deleteScan(id: Long)
}
```

`repository/scan/ScanRepositoryImpl.kt`

```kotlin
internal class ScanRepositoryImpl(
    private val scanDao: ScanDao,
) : ScanRepository {

    override suspend fun saveScan(
        rawValue: String,
        format: String,
        scannedAt: Instant,
    ): Long =
        scanDao.insert(
            entity = ScanEntity(
                rawValue = rawValue,
                format = format,
                scannedAt = scannedAt.toEpochMilliseconds(),
            ),
        )

    override fun watchScans(): Flow<List<StoredScan>> =
        scanDao.watchAll()
            .map { entities ->
                entities.map { entity ->
                    entity.toStoredScan()
                }
            }
            .flowOn(Dispatchers.Default)

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scanDao.watchById(id = id)
            .map { entity ->
                entity?.toStoredScan()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    override suspend fun deleteScan(id: Long) {
        scanDao.deleteById(id = id)
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
- Exception tant que la v2 n'est pas publiée : aucun utilisateur n'a encore la base, la version 1 du
  schéma peut donc évoluer sans migration (la table des créations du lot 10 s'y ajoute). La règle
  s'applique dès la première publication.
- Une requête de lecture multiple a un `ORDER BY` explicite : sans lui, l'ordre est un détail de
  SQLite dont l'UI finit par dépendre.
- Un index est attendu sur toute colonne utilisée en filtre ou en tri (date de scan, type).
- `@Insert(onConflict = REPLACE)` supprime puis réinsère la ligne : `@Upsert` dès qu'une ligne est
  réenregistrée (favori, renommage).
- Une requête qui retourne une relation (`@Relation`) porte `@Transaction`.

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
  (défaut `false`) et `KEY_IS_RAW_CONTENT_SHOWN` (défaut `false`).
- `SettingsRepository` expose un `watchXxx(): Flow<T>` et un `suspend fun setXxx(value: T)` par
  réglage. La valeur par défaut est une constante du Repository, appliquée à la lecture.