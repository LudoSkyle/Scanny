## Tests

Les ADR communs 0001 (FIRST) et 0002 (le code livré vient avec ses tests) du référentiel partagé
s'appliquent : tout changement qui introduit ou déplace de la logique est livré avec ses tests, dans
le même commit. Ce document dit comment les écrire dans Scanny.

Tout est testé en **test unitaire**, sans device ni émulateur : `./gradlew :<module>:allTests`
exécute la suite sur la JVM et sur le simulateur iOS. S'y ajoutent les **tests d'interface iOS**, qui
pilotent l'app sur le simulateur (voir plus bas).

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
| Lisibilité d'un code dessiné | Robolectric en rendu natif (`@GraphicsMode(NATIVE)`) + ZXing, dans `androidHostTest` de `shared` |
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
  ``fun `a wifi payload with an escaped semicolon keeps it in the password`()``. Sans virgule :
  Kotlin/Native la refuse dans un nom, et un test commun tourne aussi sur le simulateur iOS.
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

### Tests d'interface iOS

La cible `iosAppUITests` du projet Xcode (XCUITest, en Swift) lance l'app sur le simulateur et
vérifie ce qui suit un scan : la feuille, le détail, la copie, le partage, l'ouverture d'un lien dans
Safari, d'un SMS dans Messages, l'ajout d'un contact dans Contacts, la suppression, l'historique et
son effacement, le scan multiple et l'écran du groupe, les réglages (dont la copie du code après un
scan, l'ouverture de son contenu, un scan hors de l'historique, un doublon consécutif gardé une fois
et la durée de conservation), le feedback, les textes français.

- Le simulateur n'a pas de caméra : un build Debug y lit le code de la variable d'environnement
  `SCANNY_SIMULATED_CODE` comme si la caméra le voyait (voir
  [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md)). Chaque test lance l'app avec son propre code.
- La langue se fixe au lancement (`-AppleLanguages (en)`) : un test ne dépend pas de la langue du Mac.
- Les éléments se cherchent par leur libellé d'accessibilité, que Compose Multiplatform expose :
  texte d'un bouton, `contentDescription` d'une icône. Un élément cliquable réunit les textes qu'il
  contient en un seul libellé (une ligne de l'historique se cherche par « contient »), et un titre
  marqué `heading()` (celui d'un `SCCardStructure`) n'est pas un `staticText` mais un
  `otherElement`. Une ligne `toggleable` est un `switch` dont l'état se lit par `isSelected`, sans
  valeur. Un `SCDropdownField` est un `textView` nommé par son libellé ; son texte d'attente n'est pas
  exposé.
- Un appui pendant une transition entre deux écrans se perd : avant d'appuyer sur l'écran d'arrivée,
  le test attend que l'écran quitté ait disparu (`waitForNonExistence` sur un de ses textes).
- Les réglages restent d'un lancement à l'autre : un test qui en change un le remet comme il l'a
  trouvé.
- Les composants de Material ont leurs propres libellés, traduits par Compose Multiplatform : la
  feuille se ferme par « Close sheet », « Fermer la feuille » en français.
- Un état qu'aucun test ne sait produire (des scans d'autres jours) se prépare en écrivant dans la
  base de l'app sur le simulateur (`xcrun simctl get_app_container booted fr.skyle.scanny data`, puis
  `sqlite3` sur `Library/Application Support/scanny.db`), l'app arrêtée.
- Une migration de la base se vérifie de même : une base de la version précédente, écrite depuis son
  schéma exporté (ses `createSql`, `setupQueries` et `identityHash`, puis `PRAGMA user_version`),
  avec une capture récente (la purge du démarrage supprimerait une capture ancienne), remplace celle
  de l'app ; l'app lancée, la base est à la nouvelle version et la capture toujours là.
- Une ligne cliquable est un bouton dont le texte est aussi vu à part : pour compter des lignes, on
  compte les boutons (`app.buttons`).
- Une demande du système (la caméra, l'ajout aux photos) s'accepte par `allowTheSystemAlertIfAsked` :
  demandée une fois, le simulateur garde la réponse. Une image ajoutée aux photos se relit sur le Mac,
  dans le dossier `data/Media/DCIM` du simulateur.
- Les aides des tests sont communes (`UITestSupport`) : lancer l'app, trouver un élément par son
  libellé ou un champ de texte (`textInput`, vu comme une vue de texte quand il peut tenir plusieurs
  lignes), faire défiler jusqu'à un élément et attendre qu'il ne bouge plus avant de le toucher
  (`scroll`, `waitUntilStill`), y compris pendant l'arrivée d'un écran ou l'ouverture d'un menu, et
  ouvrir le détail depuis la feuille d'un scan une fois qu'elle a fini de monter (`openTheDetail`),
  ouvrir la feuille du code d'un détail (`showTheCode`) et fermer une feuille (`closeTheSheet`).
- La feuille de partage d'iOS se ferme par un glissement vers le bas : un toucher au-dessus d'elle
  fermerait aussi la feuille de l'app restée dessous.
  Après un balayage, `scroll` attend encore une seconde : une liste glisse d'une fraction de point
  après avoir l'air immobile, et Compose prend le premier toucher pour l'arrêter, sans toucher la
  ligne.
- Un code que l'app dessine se vérifie en décodant sa capture sur le Mac, avec Vision
  (`VNDetectBarcodesRequest`, depuis un court script Swift) : un second lecteur que l'app n'a pas
  écrit.
- Le sélecteur de photos du système est visible de XCUITest : une photo y est une image
  d'identifiant `PXGGridLayout-Info`, de libellé « Photo, » suivi de sa date. Un test choisit la
  première de la grille, la plus récente : une photo plus ancienne descend à chaque image que les
  tests enregistrent, jusqu'à ce que le sélecteur ne la crée plus. Il la touche à sa place
  (`coordinate`) : le sélecteur tourne dans un autre processus, où XCUITest ne sait pas toujours dire
  qu'une photo peut être touchée.
- La couleur d'un point de l'écran se lit dans la capture (`screenColor`, à une tolérance près) :
  c'est ce qui vérifie qu'une création garde les couleurs choisies. Le cadre d'une image de Compose
  est celui de l'image elle-même, sans la marge que lui donne son `padding`.
- Un test garde ses captures d'écran (`XCTAttachment`, `lifetime = .keepAlways`) : elles se relisent
  après coup, extraites du `.xcresult`.
- Le presse-papiers du simulateur se lit depuis le Mac : `xcrun simctl pbpaste booted`. Depuis le
  test, lire `UIPasteboard` ferait apparaître l'alerte de collage d'iOS : pour vérifier une copie, le
  test colle dans un champ de l'app par son menu d'édition (appui long, « Paste »), un collage que
  l'utilisateur demande.
- Un écran plus long que l'écran : une rangée hors de l'écran n'existe pas pour XCUITest, et une
  rangée coupée par le bord a un cadre rogné, qui tient dans la fenêtre. Le test fait défiler, lentement,
  jusqu'à ce que la rangée soit dégagée des bords, puis attend qu'elle ne bouge plus : un appui sur
  une liste encore en mouvement, ou sur un écran qui arrive, ne fait que l'arrêter.
- Ce que le simulateur ne sait pas faire n'est pas testé : il n'a pas l'app Mail, et n'affiche pas le
  destinataire d'un SMS (« No Name, Searching », quel que soit le lien).

### Ce que les tests ne couvrent pas

La détection des codes par la caméra et dans une image ne se teste pas sur le simulateur iOS : il n'a
pas de caméra, et Vision n'y trouve aucun code (limite du simulateur, constatée quelle que soit la
révision de la requête, dans l'app comme dans les tests Kotlin, de même que le `CIDetector` de Core
Image). Elle se vérifie à la main, sur un appareil. Tout ce qui suit la détection, en revanche, est
couvert par les tests d'interface.

La lisibilité des codes que l'app dessine se teste donc sur la JVM : `CodeImageReadabilityTest`
dessine l'image d'un QR code pour de vrai, par le rendu natif de Robolectric, et la relit avec ZXing,
un lecteur que l'app n'a pas écrit et plus strict que ceux des téléphones. Il couvre chaque forme
dans les couleurs de la palette qui ressortent le moins, et chaque paire de couleurs avec les formes
qui couvrent le moins le code, et chaque forme avec un logo. Le contraste de chaque paire de la
palette est aussi vérifié (au moins 4,5:1, celui que les règles d'accessibilité demandent d'un
texte). ZXing relit encore un code à la correction la plus faible sous un logo de cette taille : la
correction choisie pour un logo se vérifie donc directement, sur les options données à qrose
(`CodeStyleMapperTest`).

La réduction d'une image en logo se teste sur chaque plateforme : Robolectric en rendu natif sur la
JVM, y compris le chemin d'avant Android 9 (`@Config(sdk = [27])`), et le simulateur iOS, qui sait
dessiner une image.

### Accès aux `internal` depuis les tests

Les source sets de test voient les déclarations `internal` de leur module : c'est ce qui permet de
garder une API minimale entre modules **tout en** testant les DAO et les implémentations
directement.