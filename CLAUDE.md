# Scanny — instructions pour l'agent IA

Scanny est une app de scan et de création de QR codes et de codes-barres, pour Android et iOS, en
Kotlin Multiplatform et Compose Multiplatform. Elle est découpée en modules : `androidApp` et
`iosApp` (les hôtes de chaque plateforme), `shared` (écrans, navigation, ViewModels), `designSystem`
(thème et composants génériques), `domain` (modèles métier, parseur, UseCases) et `data` (base
locale, préférences, Repositories). `baselineprofile` génère les baseline profiles d'Android.

## Avant de commencer

**Lis la documentation avant toute modification de code** : elle contient les règles que ce projet
applique, et elles priment sur les habitudes générales.

Point d'entrée : [docs/INDEX.md](./docs/INDEX.md). Ne charge que le(s) document(s) utile(s) à la
tâche en cours, chacun est autonome.

| Tâche | Document à lire |
|---|---|
| Savoir où en est la migration, quoi faire ensuite | [docs/ROADMAP.md](./docs/ROADMAP.md) |
| Créer un fichier, un module, choisir une lib | [docs/Common/01_MODULES_ET_LIBRAIRIES.md](./docs/Common/01_MODULES_ET_LIBRAIRIES.md) |
| Toute contribution (nommage, style, erreurs, textes) | [docs/Common/02_CONVENTIONS.md](./docs/Common/02_CONVENTIONS.md) |
| Écrire ou modifier un test | [docs/Common/03_TESTS.md](./docs/Common/03_TESTS.md) |
| Ajouter une dépendance, toucher au build | [docs/Common/04_BUILD.md](./docs/Common/04_BUILD.md) |
| Ajouter un écran ou une route | [docs/UI/01_NAVIGATION.md](./docs/UI/01_NAVIGATION.md), [docs/UI/02_ARCHITECTURE_ECRAN.md](./docs/UI/02_ARCHITECTURE_ECRAN.md) |
| Toucher au thème ou à un composant générique | [docs/UI/03_DESIGN_SYSTEM.md](./docs/UI/03_DESIGN_SYSTEM.md) |
| Toute contribution UI Compose | [docs/UI/04_CONVENTIONS_COMPOSE.md](./docs/UI/04_CONVENTIONS_COMPOSE.md) |
| Persistance, historiques, réglages | [docs/KMP/01_MODULE_DATA.md](./docs/KMP/01_MODULE_DATA.md) |
| Logique métier, contenu d'un code, UseCase | [docs/KMP/02_MODULE_DOMAIN.md](./docs/KMP/02_MODULE_DOMAIN.md) |
| Caméra, détection, actions système, tout ce qui diffère par plateforme | [docs/KMP/03_CODE_PLATEFORME.md](./docs/KMP/03_CODE_PLATEFORME.md) |
| Toucher au projet Xcode, à l'`Info.plist`, à la signature iOS | [iosApp/CLAUDE.md](./iosApp/CLAUDE.md) |

Quatre règles à ne jamais perdre de vue :

- **Toute l'UI est en Compose Multiplatform**, dans `commonMain`. Le natif n'existe que derrière une
  frontière commune (caméra, détection, permissions, actions système). Voir
  [ADR-0002](./docs/adr/0002-ui-commune-en-compose-multiplatform.md).
- **Le domaine dépend de la donnée, jamais l'inverse** : `shared` → `domain` → `data`, et `shared`
  ne voit pas `data`. Voir [ADR-0001](./docs/adr/0001-le-domaine-depend-de-la-donnee.md).
- **`internal` par défaut** : seul ce qui traverse une frontière de module est public.
- **Pas de barre de navigation en bas** : l'app s'ouvre directement sur le scan, et tous les autres
  écrans s'ouvrent depuis lui. Voir [docs/UI/01_NAVIGATION.md](./docs/UI/01_NAVIGATION.md).

## État du projet

La migration vers KMP/CMP est en cours, lot par lot, par pull request. La fondation, l'hôte iOS, la
base du design system et le contenu des codes (`domain`) sont en place ; la suite arrive dans l'ordre
de la [feuille de route](./docs/ROADMAP.md).

## Commandes

```bash
./gradlew :domain:allTests :data:allTests :shared:allTests   # tests (JVM et simulateur iOS)
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' \
  -parallel-testing-worker-count 3                           # tests d'interface iOS (simulateur)
./gradlew :androidApp:assembleDebug                          # app Android
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64        # framework iOS
```

L'app iOS se lance depuis Xcode (`iosApp/iosApp.xcodeproj`, schéma `iosApp`) : une phase de build
y construit le framework `Shared`. Voir [iosApp/CLAUDE.md](./iosApp/CLAUDE.md).

Lance les tests du module touché avant de considérer une modification comme terminée, et les tests
d'interface iOS avant toute pull request qui touche un écran ou un parcours.

## Tests

Les ADR communs 0001 (tests FIRST) et 0002 (le code livré vient avec ses tests) du référentiel
partagé s'appliquent. Avant d'écrire un test, lis [docs/Common/03_TESTS.md](./docs/Common/03_TESTS.md) :
il porte la forme KMP (source sets, outillage). Le skill `android-testing-first` donne la structure
d'un test de UseCase, de Repository et de ViewModel (Given/When/Then, fakes) ; quand il diverge du
document du projet (JUnit 4 et Robolectric partout), c'est le document du projet qui fait foi.

Un écran se teste en Compose (`runComposeUiTest`, en code commun) ; un test d'interface iOS ne
s'écrit que pour un parcours qui passe par le système. Voir
[ADR-0003](./docs/adr/0003-les-ecrans-se-testent-en-compose.md).

## Git

- **Ne jamais ajouter de `Co-Authored-By`, de `Generated with`, ni aucune autre mention d'un outil ou
  d'un modèle d'IA** dans les messages de commit et les descriptions de pull request. L'auteur du
  commit est la personne qui l'a demandé.
- Messages de commit en anglais, préfixés par leur type : `feat:`, `fix:`, `refactor:`, `chore:`,
  `docs:`, `test:`.
- Le corps du message d'un correctif décrit le **symptôme observable**, pas l'implémentation.
- Des commits courts et mono-périmètre : une correction, un renommage ou une fonctionnalité par
  commit.
- Le dépôt privé `private/` (sous-module : clés de signature, fichiers Firebase) n'est jamais
  modifié ni affiché.
- **Chaque lot est livré par une pull request vers `develop`**, jamais poussé directement : une
  branche `<type>/<description-courte>` partie de `develop` à jour, poussée, puis la PR, fusionnée
  par l'agent lui-même **en rebase** (historique linéaire) une fois les vérifications du lot passées
  et la CI GitHub Actions verte (`gh pr checks --watch`), et la branche supprimée. Une PR qui suit une autre est rebasée sur `develop` à jour avant d'être
  poussée. Le titre de la PR est le sujet du commit principal, sa description, en anglais, dit ce qui
  change et ce qui a été vérifié.
- Rien ne se pousse en dehors de ce circuit : pas de push direct sur `develop` ni sur `main`.

## Mise à jour de cette documentation

- Toute règle d'architecture, de nommage ou de test décidée en cours de route est ajoutée au
  document correspondant dans `docs/`, pas seulement appliquée au code. Une décision structurante
  devient un ADR dans `docs/adr/`.
- La [feuille de route](./docs/ROADMAP.md) est tenue à jour dans le commit qui termine un élément.
- La documentation ne cite aucun autre projet que Scanny.