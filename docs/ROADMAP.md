# Feuille de route — Scanny v2 (KMP/CMP)

Point de départ (2026-11-20) : `develop` ne contient qu'un squelette Android (Hilt, Navigation 3,
écran Scan en cours avec CameraX). La v1 publiée sur Android sert de référence fonctionnelle : son
code est au commit `c38ffc5`.

Chaque lot est livré par une pull request vers `develop` (voir la section Git de `CLAUDE.md`). Un
élément se coche dans le commit qui le termine.

## Parité avec la v1 (minimum)

### Scan

- [x] Aperçu caméra plein écran, détection de tous les formats de `BarcodeFormat` *(détection en
      direct à vérifier sur appareil)*
- [x] Lampe torche *(à vérifier sur appareil)*
- [x] Détection dans une image de la galerie, avec un message si aucun code n'est trouvé *(iOS à
      vérifier sur iPhone)*
- [x] Écran de permission refusée, avec un accès aux réglages de l'app
- [x] Vibration après un scan (réglage) *(testée, non observée)*
- [x] Ouverture automatique des liens après un scan (réglage ; l'écran des réglages arrive au lot 10)
      *(testée, non observée)*
- [x] Détection en pause tant qu'un résultat est affiché *(testée)*

### Détail d'un scan

- [ ] Type (icône et libellé) et format du code
- [ ] Contenu formaté et contenu brut (le brut selon le réglage), chacun copiable et partageable
- [ ] Action selon le type : URL → ouvrir ; email → écrire le mail ; SMS → écrire le SMS ;
      WiFi → rejoindre le réseau (Android seulement pour l'instant) ; contact → l'ajouter aux
      contacts

### Réglages, Feedback, À propos

- [ ] Réglages : vibration après un scan, ouverture automatique des liens, affichage du contenu
      brut
- [ ] Liens : À propos, Feedback, noter l'app, politique de confidentialité, site d'Openium,
      version et build
- [ ] Feedback : choix d'un sujet (scan, détection du type, autre) et message, envoyés par mail
- [ ] À propos : version, auteur, lien GitHub, description

## Nouveautés

- [x] Zoom (pincement) et mise au point (appui) *(à vérifier sur appareil)*
- [ ] Historique des scans : liste, détail, suppression
- [ ] Scan multiple : un mode qu'on active, on passe autant de codes qu'on veut devant la caméra, on
      termine, et la liste dédoublonnée des codes trouvés s'affiche et s'enregistre comme un groupe
- [ ] Création de QR codes : texte, URL, email, SMS, WiFi
- [ ] Partager et enregistrer un QR code créé
- [ ] Historique des créations
- Pistes, non engagées : contact parmi les types créés, personnalisation d'un QR code (couleurs,
  formes), affichage des types téléphone, position et événement, avis et mise à jour depuis l'app.

## Lots techniques

Dans l'ordre. Chaque lot laisse l'app compilable sur les deux plateformes.

1. **Fondation**
   - [x] Modules `androidApp` (renommage de `app`), `shared`, `designSystem`, `domain`, `data`
   - [x] Koin à la place de Hilt, Kermit pour les logs (Crashlytics derrière sur Android)
   - [x] Compose Resources (polices Lato), `ScannyApp` commune affichée par `MainActivity`
   - [x] Navigation 3 multiplateforme (`Route`, `RootNavDisplay`)
   - [x] `compose-stability.conf` à la racine, branché sur les modules Compose
   - Les textes de la v1 (anglais et français) ne sont pas repris en bloc : chacun revient avec son
     écran, sa clé renommée selon la convention. Ils se retrouvent dans `app/src/main/res/` au
     commit `c38ffc5`.
2. **Hôte iOS**
   - [x] Projet Xcode généré par XcodeGen (`iosApp/project.yml`), app SwiftUI qui affiche
         `ScannyApp`, démarrage de Koin
   - [x] Phase de build du framework `Shared`, `Info.plist`, icône, signature locale (l'équipe Apple
         se déclare dans `Configuration/Local.xcconfig`, ignoré par git)
   - [ ] Firebase Crashlytics par SPM, branché sur les logs Kermit. *Bloqué : il faut le
         `GoogleService-Info.plist` d'une app iOS `fr.skyle.scanny` déclarée dans la console
         Firebase, à déposer dans `private/firebase/`.*
   - [x] `iosApp/CLAUDE.md` pour les règles propres au projet Xcode
3. **Design system**
   - [x] Material 3, dans `designSystem` seulement
   - [x] `SCTheme` : couleurs, Lato, formes, style de texte par défaut, ripple
   - [x] Structure d'écran (`SCScreenStructure`), barre du haut (`SCTopAppBar`), bouton icône
         (`SCIconButton`)
   - Les autres composants (bouton, dialog, champ de texte, interrupteur…) arrivent avec le premier
     écran qui en a besoin.
4. **Domain**
   - [x] `BarcodeContent`, `BarcodeFormat` (Codabar compris), `WifiEncryption`
   - [x] Parseur et encodeur, avec leurs tests, sur la JVM et le simulateur iOS
   - Le modèle `Scan` et les UseCases arrivent avec la donnée, au lot 5.
5. **Data**
   - [x] Base Room (historique des scans), schéma exporté
   - [x] DataStore des réglages, avec le fichier et les clés de la v1
   - [x] `ScanRepository`, `SettingsRepository`
   - [x] Modèle `Scan`, réglages, et les UseCases qui les exposent, avec leurs tests
   - Le câblage Koin n'est vérifié à l'exécution qu'au lot 6, quand le premier ViewModel s'en sert.
6. **Scan**
   - [x] `ScanCamera` Android et iOS, permission, torche, zoom et mise au point, scan d'une image
   - [x] Icônes des barres système claires sur l'écran noir
   - [x] `ScanViewModel` : enregistrement dans l'historique, vibration et ouverture des liens selon
         les réglages, pause de la détection, avec ses tests
   - [x] Feuille de résultat minimale : le type et le texte brut du code scanné
   - Vérifié sur l'émulateur Android : permission (refus, refus définitif, accord), aperçu, scan
     d'une image jusqu'à la base. Sur le simulateur iOS : lancement, demande de permission et sa
     description. **Reste à vérifier sur un appareil** : détection en direct, torche, zoom, mise au
     point sur Android et iPhone, et le scan d'une image sur iPhone (le simulateur n'a pas de caméra
     et Vision n'y détecte rien).
7. **Détail d'un scan et actions natives**, en deux pull requests :
   - [ ] 7a : la base et le domaine passent au modèle « capture » (voir les décisions prises), sans
         changement visible
   - [ ] 7b : la feuille rapide après un scan (type, résumé, action principale, copier, « Voir le
         détail »), l'écran `ScanDetail` (contenu formaté et brut, toutes les actions, partage,
         suppression) et les actions natives de la matrice de
         [KMP/03_CODE_PLATEFORME.md](./KMP/03_CODE_PLATEFORME.md)
   - Deux remarques d'ergonomie relevées au lot 6 : le message « aucun code trouvé » masque le bouton
     galerie le temps de son affichage, et le Photo Picker range les images de la plus récente à la
     plus ancienne.
8. **Historique des scans** : une ligne par capture ; une capture d'un code ouvre son `ScanDetail`,
   une capture de plusieurs codes ouvre l'écran du groupe, `Route.ScanGroup(scanId)`.
9. **Scan multiple** : le mode, la session de scan jusqu'à « Terminer », la feuille qui liste les
   codes trouvés (un appui ouvre le `ScanDetail` du code), l'écran du groupe.
10. **Réglages, Feedback, À propos**
11. **Création de QR codes et historique des créations**
12. **Finitions** : splash, README à jour (il cite encore Hilt et Camera 2), CI

## Décisions prises

- **Projet Xcode** : généré par XcodeGen (`iosApp/project.yml`), jamais édité à la main ; le projet
  généré est commité.
- **Identité iOS** : bundle ID `fr.skyle.scanny`, comme sur Android. Cible minimale iOS 18.5 : les
  mêmes iPhone que les iOS 17 et 18 (XS/XR et plus récents) ; en dessous, l'éditeur de liens avertit
  qu'un objet embarqué par Compose Multiplatform est compilé pour 18.5. Pas encore de compte Apple : le
  développement se fait sur simulateur, un Apple ID gratuit suffit pour un iPhone, le programme
  payant n'est nécessaire que pour TestFlight, l'App Store et le WiFi.
- **Crashlytics sur iOS** : oui, par SPM dans `iosApp`, dès que le `GoogleService-Info.plist` iOS est fourni.
- **WiFi sur iOS** : reporté jusqu'à l'obtention d'un compte développeur payant (l'entitlement
  *Hotspot Configuration* l'exige). En attendant, l'action WiFi n'est pas proposée sur iOS.
- **Génération des QR codes** : qrose (`io.github.alexzhirkevich:qrose`), la plus maintenue, la plus
  à jour et la plus riche des bibliothèques multiplateformes sans ZXing (vérifié le 2026-11-20). Elle
  rend un `Painter` en code commun, exporte en PNG sur Android et iOS, gère couleurs, dégradés,
  formes des points et des yeux, logo, niveau de correction, et sait aussi générer Data Matrix,
  Aztec, PDF417 et les codes-barres 1D (`qrose-matrix`, `qrose-oned`). Ne jamais utiliser la 1.3.0,
  qui produisait des QR codes inversés : 1.3.1 au minimum.

- **Material 3** : 1.12.0-alpha03, l'alpha compilée sur Compose Multiplatform 1.12 comme le reste de
  l'app (la dernière stable, 1.9.0, est compilée contre Compose 1.9). Seul `designSystem` en dépend,
  et le projet n'utilise aucune API Expressive. On passera à une stable dès qu'il en sortira une
  alignée sur la version de Compose.

- **Un scan est une capture de 1 à N codes** : un scan normal est une capture d'un code, un scan
  multiple une capture de tous les codes trouvés pendant la session. L'historique liste les captures,
  et retrouve ainsi un groupe complet.
- **Après un scan, une feuille rapide** reste par-dessus la caméra : un code y montre son type, un
  résumé et son action principale ; plusieurs codes (scan multiple) y sont listés. « Voir le détail »
  et un appui sur un code ouvrent l'écran `Route.ScanDetail(codeId)`, celui d'**un** code, partagé avec
  l'historique.

## Décisions ouvertes

- **« Modifier » un scan** : le nommer ou l'annoter, le marquer en favori, ou partir de son contenu
  pour créer un nouveau QR code (lot 11) ; et s'il s'agit du code, du groupe ou des deux.

- **CI** : le build iOS demande un exécuteur macOS sur CircleCI.