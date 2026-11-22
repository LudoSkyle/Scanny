# Documentation Scanny — Index

Ce document est le point d'entrée de la référence que l'agent IA suit pour ajouter, modifier ou
corriger du code dans Scanny, app Kotlin Multiplatform / Compose Multiplatform pour Android et iOS.

Ne charge que le(s) document(s) pertinent(s) pour la tâche en cours plutôt que l'ensemble : chaque
fichier ci-dessous est autonome.

Les documents sont répartis par périmètre :

- `Common/` : ce qui vaut pour tout le projet (modules, conventions, tests, build).
- `UI/` : l'UI Compose Multiplatform, commune à Android et iOS (modules `shared` et `designSystem`).
- `KMP/` : les modules Kotlin sans UI (`data`, `domain`) et le code propre à chaque plateforme.
- `adr/` : les décisions d'architecture du projet. Elles priment sur le référentiel partagé en cas
  de conflit, dans la limite de leur périmètre.

| Document | Contenu | À consulter quand... |
|---|---|---|
| [ROADMAP.md](./ROADMAP.md) | Lots de la migration, parité avec la v1, nouveautés, décisions ouvertes | Savoir quoi faire ensuite, cocher ce qui est fait |
| [Common/01_MODULES_ET_LIBRAIRIES.md](./Common/01_MODULES_ET_LIBRAIRIES.md) | Modules, graphe de dépendances, visibilité, bibliothèques | Vue d'ensemble, création d'un fichier ou d'un module, choix d'une lib |
| [Common/02_CONVENTIONS.md](./Common/02_CONVENTIONS.md) | Langue, nommage, style, gestion d'erreur, textes et traductions | Toute contribution, en complément du document ciblé |
| [Common/03_TESTS.md](./Common/03_TESTS.md) | Source sets de test, outillage, FIRST, quoi tester et à quel niveau | Écriture ou modification d'un test |
| [Common/04_BUILD.md](./Common/04_BUILD.md) | Gradle, catalogue de versions, cibles, signature, framework iOS | Ajout d'une dépendance, montée de version, configuration du build |
| [UI/01_NAVIGATION.md](./UI/01_NAVIGATION.md) | `Route.kt`, `RootNavDisplay.kt`, back stack Navigation 3 | Ajout ou modification d'une route |
| [UI/02_ARCHITECTURE_ECRAN.md](./UI/02_ARCHITECTURE_ECRAN.md) | Structure d'un écran (`entry`, `Route`, `Screen`, `ViewModel`), events one-shot | Création ou modification d'un écran, d'un ViewModel |
| [UI/03_DESIGN_SYSTEM.md](./UI/03_DESIGN_SYSTEM.md) | Module `designSystem` : `SCTheme`, composants génériques et leurs `Defaults` | Travail sur le thème ou un composant générique |
| [UI/04_CONVENTIONS_COMPOSE.md](./UI/04_CONVENTIONS_COMPOSE.md) | Signature d'un composable, état, performance, mise en page, ressources, previews | Toute contribution UI, en complément de [Common/02_CONVENTIONS.md](./Common/02_CONVENTIONS.md) |
| [KMP/01_MODULE_DATA.md](./KMP/01_MODULE_DATA.md) | Module `data` : Room, DataStore, Repositories | Persistance, historiques, réglages |
| [KMP/02_MODULE_DOMAIN.md](./KMP/02_MODULE_DOMAIN.md) | Module `domain` : modèles métier, parseur et encodeur, UseCases | Logique métier, interprétation ou création du contenu d'un code |
| [KMP/03_CODE_PLATEFORME.md](./KMP/03_CODE_PLATEFORME.md) | `expect`/`actual`, interfaces natives, matrice Android/iOS | Caméra, détection, permissions, actions système |
| [adr/0001-le-domaine-depend-de-la-donnee.md](./adr/0001-le-domaine-depend-de-la-donnee.md) | `shared` → `domain` → `data`, exceptions typées | Toute intervention sur `data` ou `domain` |
| [adr/0002-ui-commune-en-compose-multiplatform.md](./adr/0002-ui-commune-en-compose-multiplatform.md) | UI 100 % Compose Multiplatform, natif derrière une frontière | Toute question « commun ou natif ? » |
| [adr/0003-les-ecrans-se-testent-en-compose.md](./adr/0003-les-ecrans-se-testent-en-compose.md) | Écrans testés en Compose, XCUITest réservé aux parcours système | Tout test d'écran |
| [adr/0004-encodeurs-data-matrix-et-aztec-corriges.md](./adr/0004-encodeurs-data-matrix-et-aztec-corriges.md) | Encodeurs Data Matrix et Aztec copiés de qrose et corrigés, latin-1 seulement | Tout travail sur le dessin d'un code en deux dimensions |

## Résumé rapide des modules

- `androidApp` : hôte Android (`Application`, `MainActivity`, manifest, icône, signature, Firebase).
- `iosApp` : hôte iOS (projet Xcode, app SwiftUI qui affiche les écrans de `shared`, `Info.plist`).
- `shared` : écrans, navigation, ViewModels, composants « produit », code natif des écrans
  (caméra, actions système). Produit le framework `Shared` pour iOS.
- `designSystem` : thème (`SCTheme`) et composants génériques, sans connaissance du métier.
- `domain` : modèles métier, parseur et encodeur du contenu des codes, UseCases. Kotlin pur.
- `data` : base Room, préférences DataStore, Repositories.

Voir [Common/01_MODULES_ET_LIBRAIRIES.md](./Common/01_MODULES_ET_LIBRAIRIES.md) pour le détail.