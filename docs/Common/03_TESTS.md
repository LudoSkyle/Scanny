## Tests

Les ADR communs 0001 (FIRST) et 0002 (le code livré vient avec ses tests) du référentiel partagé
s'appliquent : tout changement qui introduit ou déplace de la logique est livré avec ses tests, dans
le même commit. Ce document dit comment les écrire dans Scanny.

Tout est testé en **test unitaire**, sans device ni émulateur : `./gradlew :<module>:allTests`
exécute la suite sur la JVM et sur le simulateur iOS.

| Source set | Code testé | Exécuté sur |
|---|---|---|
| `src/commonTest` | `commonMain` | la JVM (tests hôtes Android) **et** le simulateur iOS |
| `src/androidHostTest` | `androidMain` | la JVM, avec Robolectric si un `Context` est nécessaire |
| `src/iosTest` | `iosMain` | le simulateur iOS (`iosSimulatorArm64Test`, Mac avec Xcode) |

Un test de code commun va dans `commonTest` : c'est ce qui vérifie que le code se comporte pareil sur
les deux plateformes.

### Outillage

| Besoin | Outil |
|---|---|
| Framework | `kotlin.test` (`@Test`, `assertEquals`, `assertFailsWith`) |
| Coroutines | `kotlinx-coroutines-test` (`runTest`, `UnconfinedTestDispatcher`) |
| Room | vraie base en mémoire, `BundledSQLiteDriver` |
| Android sur la JVM | Robolectric + `androidx.test:core-ktx`, seulement dans `androidHostTest` |
| Composants Compose | `runComposeUiTest`, qui tourne sur la JVM et sur le simulateur iOS |
| Doublures | Fakes écrits à la main, dans un package `fake/` du source set de test |

Pas de framework de mock : les interfaces sont petites, un fake qui enregistre les appels est plus
lisible et ne casse pas au premier refactor.

### Que tester, et à quel niveau

| Niveau | Testé avec | Ce qu'on vérifie |
|---|---|---|
| Parseur et encodeur (`domain`) | `kotlin.test` seul | chaque format de contenu, les champs absents, les caractères échappés, l'aller-retour encodage → parsing sans perte |
| UseCase (`domain`) | fakes des Repositories de `data` | orchestration, règles métier, exceptions typées levées |
| Repository et DAO (`data`) | vraie base Room en mémoire | mapping entité ↔ modèle, tri, filtres, suppression |
| ViewModel (`shared`) | fakes des UseCases et des interfaces natives | état exposé, events émis, erreurs attrapées |
| Composant avec de la logique d'affichage | `runComposeUiTest` | ce qui s'affiche selon l'état |

- **Tout UseCase, Repository et ViewModel est livré avec son fichier de test**, nommé comme lui et
  suffixé `Test` (`RecordScanUseCaseTest`), dans le même package du source set de test.
- **Une requête SQL se teste contre une vraie base**, jamais contre un faux DAO : c'est le SQL qui
  contient les bugs.
- Le parseur est le cœur de la parité entre Android et iOS : chaque type de contenu reconnu a ses
  tests, avec des exemples réels de codes (WiFi avec `;` échappé, vCard multi-lignes, MeCard…).
- Une `Preview` n'est pas un test.

### Règles FIRST

- **Fast** — en mémoire, sans réseau ni fichier. Base Room en mémoire.
- **Independent** — chaque test passe seul et dans n'importe quel ordre, sans état partagé.
- **Repeatable** — pas de dépendance à l'horloge ni à l'aléatoire : le code testé reçoit un
  `Clock` en paramètre, et les tests lui passent une date fixe.
- **Self-validating** — le test conclut seul par des assertions, jamais par la lecture d'un log.
- **Timely** — le test s'écrit avec le code. Un correctif de bug **commence** par un test qui
  reproduit le bug et échoue.

### Nommage et structure

- Nom de test en backticks, descriptif et en anglais :
  ``fun `a wifi payload with an escaped semicolon keeps it in the password`()``.
- Corps en trois temps, séparés par une ligne vide et marqués `// Given`, `// When`, `// Then`.
- Les constructeurs d'objets de test passent par un helper privé avec des valeurs par défaut, pour
  que chaque test ne mette en avant que ce qui lui importe.
- Une classe de base porte la mise en place commune (`DatabaseTest`) : un test ne répète pas quinze
  lignes de construction.

```kotlin
class RecordScanUseCaseTest {

    private val scanRepository = FakeScanRepository()

    private val recordScanUseCase = RecordScanUseCaseImpl(
        scanRepository = scanRepository,
        clock = FixedClock(instant = SCANNED_AT),
    )

    @Test
    fun `a code read several times is recorded once at its first reading`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(rawValue = "First"),
                barcode(rawValue = "Second"),
                barcode(rawValue = "First"),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                "First" to "QR_CODE",
                "Second" to "QR_CODE",
            ),
            actual = recordedCodes(),
        )
    }

    private fun recordedCodes(): List<Pair<String, String>> =
        scanRepository.storedScans
            .single()
            .codes
            .map { code ->
                code.rawValue to code.format
            }

    private fun barcode(
        rawValue: String,
        format: BarcodeFormat = BarcodeFormat.QR_CODE,
    ): DetectedBarcode =
        DetectedBarcode(
            rawValue = rawValue,
            format = format,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
```

### Contraintes des tests communs

Kotlin/Native impose quelques règles au code de `commonTest` :

- Pas de virgule dans un nom de test.
- Une classe de base ne prépare rien dans un `@BeforeTest` : Kotlin/Native ne l'exécute pas
  forcément avant celui de la sous-classe. Elle prépare à la construction de l'instance de test.
- Ce qui dépend de la plateforme (construire une base Room en mémoire, qui demande un `Context` sur
  Android) passe par un `expect` de test, implémenté dans `androidHostTest` (Robolectric) et
  `iosTest`.
- Une exception attendue se vérifie avec `assertFailsWith<T> { }`.
- Un ViewModel se teste après `Dispatchers.setMain(UnconfinedTestDispatcher())`. Tant que l'ordre
  d'exécution d'un `@AfterTest` hérité n'est pas vérifié sur le simulateur iOS, le `setMain` et le
  `Dispatchers.resetMain()` (dans un `@AfterTest`) se déclarent dans la classe de test elle-même.
- Un texte traduit se lit depuis sa ressource (`getString(Res.string.x)`), jamais en dur : le
  simulateur iOS parle la langue du Mac, la JVM parle anglais.

### Base de données, réglages et `Flow`

- **Room** : `DatabaseTest` (commun) construit une vraie base en mémoire à partir d'un `expect`,
  implémenté avec Robolectric dans `androidHostTest` et sans rien de plus dans `iosTest`. Le SDK que
  simule Robolectric est fixé dans `src/androidHostTest/resources/robolectric.properties` : le
  `compileSdk` est plus récent que ceux qu'il sait simuler.
- Le SQLite embarqué (`BundledSQLiteDriver`) charge ses bibliothèques natives : sur la JVM des tests
  hôtes, `data/build.gradle.kts` extrait celles de `sqlite-bundled-jvm` et les place sur le
  `java.library.path`. Sans cela, un test de base échoue sur un `UnsatisfiedLinkError`.
- **DataStore** : un test construit son `DataStore<Preferences>` sur un système de fichiers en
  mémoire (`FakeFileSystem` d'okio, `OkioStorage` et `PreferencesSerializer`), nouveau à chaque
  test et lié au `backgroundScope` de `runTest`. Aucun fichier réel n'est écrit.
- Room émet la valeur courante dès la collecte : `dao.watchAll().first()` suffit dans la majorité
  des cas, sans dépendance de test supplémentaire.
- Pour vérifier ce qu'un `Flow` réémet après un changement, le test le collecte dans une liste,
  depuis le `backgroundScope` avec un `UnconfinedTestDispatcher(testScheduler)`, puis fait le
  changement et compare la liste.

### ViewModels

- Un test de ViewModel pose `Dispatchers.setMain` sur l'`UnconfinedTestDispatcher` qu'il passe à
  `runTest`, dans un `@BeforeTest` de la classe de test : le temps est alors virtuel, et un `delay` du
  ViewModel s'avance avec `advanceTimeBy` puis `runCurrent`.
- Ses events (`Channel`) se collectent dans une liste, comme un `Flow` (voir ci-dessus).
- Pour vérifier ce qui se passe pendant un appel en cours (une deuxième détection pendant un
  enregistrement), la doublure attend un `CompletableDeferred` que le test complète quand il veut.
  Une doublure qui répond tout de suite ne fait jamais apparaître la course.
- Dans `shared`, `withHostTest { isReturnDefaultValues = true }` : les logs de Kermit passent par
  `android.util.Log`, absent de la JVM des tests hôtes.
- Un test qui croit vérifier un comportement le prouve en échouant quand on casse ce comportement :
  avant de livrer, on retire le code protégé, on voit le test échouer, on le remet.

### Ce que les tests ne couvrent pas

La détection des codes par la caméra et dans une image ne se teste pas sur le simulateur iOS : il n'a
pas de caméra, et Vision n'y trouve aucun code (limite du simulateur, constatée quelle que soit la
révision de la requête). Elle se vérifie à la main, sur un appareil.

### Accès aux `internal` depuis les tests

Les source sets de test voient les déclarations `internal` de leur module : c'est ce qui permet de
garder une API minimale entre modules **tout en** testant les DAO et les implémentations
directement.