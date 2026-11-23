# ADR-0003 — Les écrans se testent en Compose, XCUITest ne garde que le système

Status: Accepted — 2026-11-22

> ADR **de projet**. Vaut pour tout le dépôt.

## Contexte

Les écrans ont d'abord été vérifiés par des tests d'interface iOS (XCUITest) : 39 tests, douze
minutes par passage. Chacun lance l'app, attend ses transitions et touche à travers l'accessibilité
d'iOS : quinze à vingt secondes par test, et des échecs qui viennent du simulateur (un toucher perdu
pendant une animation) plutôt que de l'app. La plupart ne vérifiaient pourtant que ce qu'un écran
affiche selon son état, ce qu'un écran Compose sans état montre seul.

Compose Multiplatform fournit `runComposeUiTest`, qui tourne en code commun : sur la JVM avec
Robolectric, et sur le simulateur iOS sans lancer l'app.

## Décision

- **Ce qu'un écran affiche et les rappels qu'il déclenche se testent en Compose**, dans
  `commonTest` de `shared`, sur la fonction sans état de l'écran (voir
  [Common/03_TESTS.md](../Common/03_TESTS.md)). Ces tests tournent avec `allTests`, sur la JVM et le
  simulateur iOS.
- **XCUITest ne garde que ce que seule l'app entière montre** : une autre app ouverte (Safari,
  Messages, Contacts), une feuille ou un sélecteur du système, le presse-papiers, Photos, un geste
  d'iOS, un parcours de bout en bout qui traverse la base et la navigation, et les textes français.
- La suite XCUITest tourne en parallèle sur trois clones du simulateur, et n'attend jamais une alerte
  du système qui ne vient pas.

## Conséquences

- Un écran nouveau vient avec son test Compose, pas avec un test d'interface iOS.
- La suite XCUITest reste courte, une quinzaine de tests, quelques minutes en parallèle.
- Les tests Compose parlent la langue du Mac : les textes français ne sont vérifiés que par
  XCUITest, qui fixe la langue au lancement.
- Un test Compose ne voit ni Koin ni la navigation : leur câblage reste vérifié par les parcours de
  bout en bout de XCUITest et par les tests de ViewModel.
