# Feuille de route — Scanny v2 (KMP/CMP)

Point de départ (2026-11-20) : `develop` ne contient qu'un squelette Android (Hilt, Navigation 3,
écran Scan en cours avec CameraX). La v1 publiée sur Android sert de référence fonctionnelle : son
code est au commit `c38ffc5`.

La migration se fait directement sur `develop`. Un élément se coche dans le commit qui le termine.

## Parité avec la v1 (minimum)

### Scan

- [ ] Aperçu caméra plein écran, détection de tous les formats de `BarcodeFormat`
- [ ] Lampe torche
- [ ] Détection dans une image de la galerie, avec un message si aucun code n'est trouvé
- [ ] Écran de permission refusée, avec un accès aux réglages de l'app
- [ ] Vibration après un scan (réglage)
- [ ] Ouverture automatique des liens après un scan (réglage)
- [ ] Détection en pause tant qu'un résultat est affiché

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

- [ ] Zoom (pincement) et mise au point (appui)
- [ ] Historique des scans : liste, détail, suppression
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
2. **Hôte iOS** *(reste bloqué par l'identité iOS, voir les décisions ouvertes)*
   - [ ] Projet Xcode généré par XcodeGen (`iosApp/project.yml`), app SwiftUI qui affiche
         `ScannyApp`, démarrage de Koin
   - [ ] Phase de build du framework `Shared`, `Info.plist`, icône, signature
   - [ ] Firebase Crashlytics par SPM, branché sur les logs Kermit
   - [ ] `iosApp/CLAUDE.md` pour les règles propres au projet Xcode
3. **Design system** : `SCTheme` (couleurs, Lato, formes), structure d'écran, puis les composants au
   fil des écrans
4. **Domain** : `BarcodeContent`, `BarcodeFormat`, parseur et encodeur, avec leurs tests
5. **Data** : base Room, DataStore (avec reprise des réglages de la v1), Repositories
6. **Scan** : `ScanCamera` Android puis iOS, permission, torche, galerie, zoom et mise au point.
   Un premier essai CameraX (sur l'ancienne base Hilt) est gardé comme exemple dans le stash
   « WIP scan screen (CameraX + Hilt)… » (`git stash list`) : il se consulte, il ne s'applique pas.
7. **Détail d'un scan et actions natives** : interfaces de plateforme de la matrice de
   [KMP/03_CODE_PLATEFORME.md](./KMP/03_CODE_PLATEFORME.md)
8. **Historique des scans**
9. **Réglages, Feedback, À propos**
10. **Création de QR codes et historique des créations**
11. **Finitions** : splash, README à jour (il cite encore Hilt et Camera 2), CI

## Décisions prises

- **Projet Xcode** : généré par XcodeGen (`iosApp/project.yml`), jamais édité à la main.
  L'installation des outils iOS nécessaires est acceptée.
- **Crashlytics sur iOS** : oui, par SPM dans `iosApp`, au lot 2.
- **WiFi sur iOS** : reporté jusqu'à l'obtention d'un compte développeur payant (l'entitlement
  *Hotspot Configuration* l'exige). En attendant, l'action WiFi n'est pas proposée sur iOS.
- **Génération des QR codes** : qrose (`io.github.alexzhirkevich:qrose`), la plus maintenue, la plus
  à jour et la plus riche des bibliothèques multiplateformes sans ZXing (vérifié le 2026-11-20). Elle
  rend un `Painter` en code commun, exporte en PNG sur Android et iOS, gère couleurs, dégradés,
  formes des points et des yeux, logo, niveau de correction, et sait aussi générer Data Matrix,
  Aztec, PDF417 et les codes-barres 1D (`qrose-matrix`, `qrose-oned`). Ne jamais utiliser la 1.3.0,
  qui produisait des QR codes inversés : 1.3.1 au minimum.

## Décisions ouvertes

- **Identité iOS** : bundle ID, team Apple, version iOS minimale.
- **Material 3 (lot 3)** : la dernière version stable pour Compose Multiplatform (1.9.0) est en
  retard sur Compose Multiplatform 1.12 ; les versions alignées sont des alphas (1.12.0-alpha03).
  À trancher quand le design system enveloppera son premier composant Material.
- **CI** : le build iOS demande un exécuteur macOS sur CircleCI.