## Build et dépendances

### Catalogue de versions

Toutes les versions et dépendances sont déclarées dans `gradle/libs.versions.toml`. Aucune version
en dur dans un `build.gradle.kts`.

- Une entrée inutilisée est supprimée du catalogue : elle fait croire que la techno est en place.
- Un commentaire accompagne toute version contrainte, avec la raison (ex. : la version de Compose
  Multiplatform qui correspond au `composeBom` utilisé par l'app Android).
- La cible Java/Kotlin est alignée sur **tous** les modules (JVM 17).
- Le bloc `dependencies` d'un module est trié et commenté par famille (Compose, Lifecycle, Koin,
  Room, Tests…).

### Plugins par module

AGP 9 interdit d'appliquer `com.android.application` à un module Kotlin Multiplatform : l'app
Android est un module à part, qui ne fait qu'héberger `shared`.

Les plugins Gradle de tous les modules sont sur le classpath de `buildSrc`
(`buildSrc/build.gradle.kts`), avec leur version venue du catalogue. Un module les applique par leur
id, sans version, et le `build.gradle.kts` racine n'a pas de bloc `plugins` : un plugin déclaré à la
fois à la racine et dans `buildSrc` ne se charge pas.

La configuration commune des modules KMP passe par deux plugins de convention de `buildSrc` :

- `scanny-kmp-library` : `org.jetbrains.kotlin.multiplatform`, `com.android.kotlin.multiplatform.library`,
  cibles `iosArm64` et `iosSimulatorArm64`, `compileSdk` et `minSdk` du catalogue ;
- `scanny-compose-library` : la même chose, plus `org.jetbrains.compose`,
  `org.jetbrains.kotlin.plugin.compose`, les ressources Android (Compose Resources en a besoin), le
  fichier de stabilité et `ui-tooling` pour les previews d'Android Studio.

| Module | Plugins |
|---|---|
| `androidApp` | `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, Firebase (`com.google.gms.google-services`, `com.google.firebase.crashlytics`, `com.google.firebase.appdistribution`), `com.starter.easylauncher` |
| `shared` | `scanny-compose-library`, `org.jetbrains.kotlin.plugin.serialization` |
| `designSystem` | `scanny-compose-library` |
| `domain` | `scanny-kmp-library` |
| `data` | `scanny-kmp-library`, `com.google.devtools.ksp`, `androidx.room` |

Une configuration qui se répète dans plusieurs modules va dans un plugin de convention plutôt que
d'être copiée. Les fonctions `catalogVersion` et `catalogLibrary` (`ScannyCatalog.kt`) y lisent le
catalogue, dont les accesseurs typés n'existent pas dans `buildSrc`.

### Configuration d'un module KMP

- Cibles : `android`, `iosArm64()` (iPhone) et `iosSimulatorArm64()` (simulateur sur Mac Apple
  Silicon). Pas d'`iosX64()` : les Mac Intel ne sont pas une cible de développement.
- `minSdk` et `compileSdk` viennent du catalogue.
- Une dépendance disponible sur toutes les plateformes se déclare dans `commonMain`, les autres dans
  le source set de leur plateforme (`androidMain`, `iosMain`).
- Un processeur KSP se déclare pour chaque cible (`kspAndroid`, `kspIosArm64`,
  `kspIosSimulatorArm64`).
- Les tests hôtes se déclarent dans le module (`withHostTest { }`) : AGP n'accepte qu'un seul appel,
  un plugin de convention ne peut pas le faire à sa place.
- Les schémas Room sont exportés (`room { schemaDirectory("$projectDir/schemas") }`) et versionnés.
- Les modules Compose déclarent le package de leur classe `Res`
  (`compose.resources { packageOfResClass = "..." }`), qui reste `internal` au module.
- Le fichier de stabilité partagé, `compose-stability.conf` à la racine, est branché par
  `scanny-compose-library` (`composeCompiler { stabilityConfigurationFiles.add(...) }`).

### Android (`androidApp`)

- `applicationId` : `fr.skyle.scanny` ; la variante debug ajoute `.debug` et le suffixe de version
  `-debug`, et porte un ruban « DEBUG » sur l'icône (easylauncher).
- La version (`APP_VERSION_NAME`, `versionName`) est déclarée dans `androidApp/build.gradle.kts`,
  et se change par une pull request. `versionCode` en découle, le même à chaque build : majeure ×
  10 000 + mineure × 100 + correctif, `2.0` donnant 20000, `2.1.3` 20103. Une version envoyée au
  Play Store se promeut de piste en piste dans la Play Console, sans être renvoyée.
- La signature lit `private/keys/keystore.properties`, et `google-services.json` est copié depuis
  `private/firebase/` avant le build. `private/` est un sous-module privé : il n'est jamais modifié,
  ni son contenu affiché.
- Sans le sous-module (un fork, la CI des PR), le build se configure et teste quand même : le debug
  est signé avec la clé par défaut d'Android, la release n'est pas signée, et Google Services et
  Crashlytics ne s'appliquent pas ; l'app démarre alors sans Crashlytics.
- La release est minifiée (`isMinifyEnabled`, `isShrinkResources`) : toute classe instanciée par
  réflexion est gardée dans `proguard-rules.pro`.

### Baseline profiles (Android)

- Le module `baselineprofile` (`com.android.test`, plugin `androidx.baselineprofile`) pilote la
  release sur un appareil : le démarrage jusqu'à l'écran de scan, qui fait aussi le profil de
  démarrage (le code rangé en tête du dex), puis l'éditeur et l'historique. Les profils générés sont
  commités dans `androidApp/src/release/generated/baselineProfiles/` ; la release les embarque, et
  `profileinstaller` les compile à l'installation hors du Play Store.
- Ils se régénèrent quand un parcours change, sur un **émulateur** d'Android 13 ou plus, en anglais :
  `ANDROID_SERIAL=emulator-5554 ./gradlew :androidApp:generateReleaseBaselineProfile`. Jamais sur un
  téléphone : la release qu'il installe remplacerait celle du Play Store.
- `StartupBenchmark` mesure le démarrage à froid sans compilation et avec le profil :
  `ANDROID_SERIAL=emulator-5554 ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`.
  Sur l'émulateur Android 17, le 2026-11-23 : médiane de 782 ms sans, 730 ms avec.

### iOS (`iosApp`)

- `shared` produit un framework **statique** nommé `Shared` pour `iosArm64` et `iosSimulatorArm64`.
- Le projet Xcode le construit et l'intègre par une phase de build qui lance
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
- Le projet Xcode est généré par XcodeGen depuis `iosApp/project.yml`, seul fichier à éditer : le
  `.pbxproj` ne se modifie jamais à la main, il se régénère (`xcodegen generate`). Le projet généré
  est commité.
- Au démarrage, l'icône de l'app sur le blanc de l'icône : sur Android par `core-splashscreen`
  (`Theme.Scanny.Starting`, `installSplashScreen()` dans `MainActivity`), sur toutes les versions ;
  sur iOS par `UILaunchScreen` de l'`Info.plist` (la couleur `LaunchBackground` et l'image
  `LaunchLogo` des assets).
- Bundle ID `fr.skyle.scanny`, cible minimale iOS 18.5, iPhone en portrait. `MARKETING_VERSION` et
  `CURRENT_PROJECT_VERSION` suivent `versionName` et `versionCode` d'Android.
- Aucune équipe Apple n'est commitée : l'équipe personnelle se déclare dans
  `iosApp/Configuration/Local.xcconfig`, ignoré par git. Le détail est dans
  [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md).

### Intégration continue

- GitHub Actions, `.github/workflows/ci.yml` : à chaque pull request et à chaque push sur `develop`
  et `main`, sur un runner `macos-26` (le Xcode du projet, le SDK Android) : `:domain:allTests`,
  `:data:allTests`, `:shared:allTests` (JVM et simulateur iOS), `:androidApp:assembleDebug`, puis
  l'app iOS (`xcodebuild build` sur le simulateur iPhone 17, sans signature : un simulateur générique
  compilerait aussi pour x86_64, que le framework ne cible pas). Les rapports de tests sont gardés en cas d'échec.
- Sans le sous-module privé, qu'un fork n'atteint pas : le build s'en passe (voir Android
  ci-dessus). Les tests d'interface iOS (XCUITest) n'y tournent pas.
- Une pull request n'est fusionnée qu'une fois la CI verte.
### Publication Android

- `.github/workflows/release-android.yml`, lancé à la main : Actions > Release Android > Run
  workflow, depuis `main`, en choisissant la piste du Play Store (interne par défaut, alpha, bêta,
  production) et l'état (déployé tout de suite, ou brouillon à déployer depuis la Play Console).
- Il récupère le sous-module privé avec une clé de déploiement en lecture seule (secret
  `PRIVATE_SUBMODULE_SSH_KEY`, la clé publique « Scanny CI (read only) » sur le dépôt privé), construit
  le bundle et l'APK signés (`:androidApp:bundleRelease`, `:androidApp:assembleRelease`), les garde
  avec le mapping en artefact, et l'envoie au Play Store avec le compte de service de la
  Play Console (secret `PLAY_SERVICE_ACCOUNT_JSON`). Il publie enfin ces mêmes fichiers dans la
  release GitHub `vX.Y` de la version (`scanny-X.Y.aab`, `scanny-X.Y.apk`), créée sur le commit
  publié, ou complétée si elle existe.
- `.github/workflows/distribute-android.yml`, lancé à la main (Actions > Distribute Android) :
  l'app signée, en release ou en debug, envoyée aux testeurs de Firebase App Distribution (groupes,
  « Internal » par défaut, et notes de version au choix), avec le compte de service du projet
  Firebase (secret `FIREBASE_SERVICE_ACCOUNT_JSON`), écrit dans `androidApp/google-services-account.json`
  le temps du build : ce fichier est ignoré par git.
- Un workflow lancé à la main n'apparaît dans l'onglet Actions qu'une fois sur la branche par défaut.
- iOS n'a pas encore de publication : il manque le compte développeur payant et le
  `GoogleService-Info.plist`.

### Commandes utiles

```bash
./gradlew :domain:allTests                       # tests d'un module (JVM et simulateur iOS)
./gradlew :androidApp:assembleDebug              # app Android
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64   # vérifie que le framework iOS compile
```