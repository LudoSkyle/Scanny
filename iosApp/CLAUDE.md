# CLAUDE.md — hôte iOS

Périmètre : l'hôte iOS (`iosApp/`) — projet Xcode, app SwiftUI, `Info.plist`, assets, réglages de
build. Ne couvre ni les écrans (dans `shared`, voir [../docs/INDEX.md](../docs/INDEX.md)), ni le code
natif iOS des écrans (dans `shared/src/iosMain`, voir
[../docs/KMP/03_CODE_PLATEFORME.md](../docs/KMP/03_CODE_PLATEFORME.md)).

## Règles

- **Le projet Xcode est généré par XcodeGen.** Seul `project.yml` s'édite, puis
  `xcodegen generate` se lance depuis `iosApp/`. Le `.pbxproj` ne se modifie jamais à la main ni par
  l'interface de Xcode : il serait écrasé à la génération suivante. Le projet généré est commité,
  pour s'ouvrir directement, et régénéré dans le commit qui change `project.yml`.
- **Aucun écran en SwiftUI** ([ADR-0002](../docs/adr/0002-ui-commune-en-compose-multiplatform.md)).
  L'app ne fait que démarrer Koin (`KoinInitializerKt.doInitKoin()`) et afficher
  `MainViewControllerKt.MainViewController()`. Du Swift ne s'ajoute que pour une API inaccessible
  depuis Kotlin, fournie à `shared` au démarrage derrière une interface commune.
- La phase de build « Compile Kotlin Framework » lance
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode` : elle construit le framework statique
  `Shared` et copie les Compose Resources dans le bundle (`compose-resources/`). Elle a besoin de
  `ENABLE_USER_SCRIPT_SANDBOXING = NO`.
- `MARKETING_VERSION` et `CURRENT_PROJECT_VERSION` de `project.yml` suivent `versionName` et
  `versionCode` de `androidApp`.
- Cible minimale : **iOS 18.5**. Elle tourne sur les mêmes iPhone que les iOS 17 et 18 (XS/XR de 2018
  et plus récents). En dessous, l'éditeur de liens avertit qu'un objet embarqué par Compose
  Multiplatform (les données ICU de son moteur graphique) est compilé pour iOS 18.5.
- iPhone seulement, en portrait, comme l'app Android.
- **Signature** : aucune équipe Apple dans le dépôt. Le simulateur signe localement, sans équipe.
  Pour un iPhone, l'équipe (`DEVELOPMENT_TEAM = ...`, un Apple ID gratuit suffit) se déclare dans
  `Configuration/Local.xcconfig`, ignoré par git, inclus par `Configuration/Scanny.xcconfig`.
- Toute API qui demande une autorisation déclare sa clé d'usage dans `iosApp/Info.plist`, traduite en
  anglais et en français. `CADisableMinimumFrameDurationOnPhone` reste à `true` : Compose
  Multiplatform s'arrête sans elle.

## Commandes

```bash
cd iosApp && xcodegen generate     # après toute modification de project.yml
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17' build   # depuis la racine du dépôt
```