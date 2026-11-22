# Feuille de route — Scanny v2 (KMP/CMP)

Point de départ (2026-11-20) : `develop` ne contient qu'un squelette Android (Hilt, Navigation 3,
écran Scan en cours avec CameraX). La v1 publiée sur Android sert de référence fonctionnelle : son
code est au commit `c38ffc5`.

Chaque lot est livré par une pull request vers `develop` (voir la section Git de `CLAUDE.md`). Un
élément se coche dans le commit qui le termine.

## Parité avec la v1 (minimum)

### Scan

- [x] Aperçu caméra plein écran, détection de tous les formats de `BarcodeFormat` *(détection en
      direct vérifiée sur un Galaxy S21 FE ; iOS à vérifier sur iPhone)*
- [x] Lampe torche *(à vérifier sur appareil)*
- [x] Détection dans une image de la galerie, avec un message si aucun code n'est trouvé *(iOS à
      vérifier sur iPhone)*
- [x] Écran de permission refusée, avec un accès aux réglages de l'app
- [x] Vibration après un scan (réglage) *(testée, non observée)*
- [x] Ouverture automatique du contenu après un scan (réglage) : lien, e-mail, SMS, contact,
      réseau Wi-Fi *(lien observé sur le simulateur iOS)*
- [x] Détection en pause tant qu'un résultat est affiché *(testée)*

### Détail d'un scan

- [x] Type (icône et libellé) et format du code
- [x] Contenu formaté et contenu brut (le brut selon le réglage), chaque partie copiable, le code
      partageable (sa valeur brute)
- [x] Action selon le type : URL → ouvrir ; email → écrire le mail ; SMS → écrire le SMS ;
      WiFi → rejoindre le réseau (Android 11 et plus, pas en WEP ; iOS plus tard) ; contact →
      l'ajouter aux contacts

### Réglages, Feedback, À propos

- [x] Réglages : vibration après un scan, ouverture automatique des liens, affichage du contenu
      brut
- [x] Liens : Feedback, noter l'app (Android ; iOS une fois l'app sur l'App Store), politique de
      confidentialité, site d'Openium, version et build
- [x] Feedback : choix d'un sujet (scan, détection du type, autre) et message, envoyés par mail
- [x] À propos : version, auteur, lien GitHub, description, dans l'en-tête et les liens des réglages
      (l'écran À propos de la v1 n'existe plus)

## Nouveautés

- [x] Zoom (pincement) et mise au point (appui) *(zoom vérifié sur un Galaxy S21 FE ; mise au point
      à vérifier)*
- [x] Historique des scans : liste, détail, suppression ; l'enregistrer ou non, ignorer les doublons
      consécutifs, durée de conservation (30 jours par défaut, ou toujours)
- [x] Scan multiple : un mode qu'on active, on passe autant de codes qu'on veut devant la caméra, on
      termine, et la liste dédoublonnée des codes trouvés s'affiche et s'enregistre comme un groupe
- [x] Bip après un scan et copie du code après un scan (réglages) *(bip testé, non entendu)*
- [x] Création de QR codes : texte, URL, email, SMS, WiFi *(et contact, codes-barres)*
- [x] Personnalisation d'un QR code : couleurs, logo au centre, formes
- [x] Modifier un code scanné : reprendre son contenu, le modifier, le personnaliser et générer le
      nouveau QR code
- [x] Partager et enregistrer un QR code créé *(enregistrer : Android 10 et plus)*
- [x] Historique des créations
- [ ] Favori et annotation d'une capture, scan simple ou groupe entier *(pas urgent)*
- Pistes, non engagées : contact parmi les types créés, affichage des types téléphone, position et
  événement, avis et mise à jour depuis l'app.

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
7. **Détail d'un scan et actions natives**, en trois pull requests :
   - [x] 7a : la base et le domaine passent au modèle « capture » (voir les décisions prises), sans
         changement visible. Vérifié sur l'émulateur Android : le scan d'une image enregistre une
         capture et son code
   - [x] 7b : la feuille rapide après un scan (type, résumé, action principale, copier, « Voir le
         détail »), l'écran `ScanDetail` (contenu par parties et brut, copie, partage, suppression)
         et les actions qui passent par Compose : ouvrir un lien, écrire un e-mail ou un SMS.
         Vérifié sur l'émulateur Android. Son action « Modifier » arrive avec l'éditeur, au lot 11
   - [x] 7c : les actions natives qui ouvrent un écran du système : ajouter un contact (Android et
         iOS), rejoindre un réseau WiFi (Android ; `ACTION_WIFI_ADD_NETWORKS` demande Android 11,
         le WEP n'y est pas pris en charge). Vérifié sur l'émulateur Android
   - [x] Tests d'interface iOS (`iosAppUITests`) : sur le simulateur, une caméra simulée lit le code
         donné au lancement ; la feuille, le détail, la copie, le partage, Safari, Messages, Contacts,
         la suppression et les textes français y sont vérifiés
8. **Historique des scans**
   - [x] `Route.ScanHistory`, ouvert depuis le scan (icône en haut à droite) : une ligne par capture,
         par jour (aujourd'hui, hier, puis la date), de la plus récente à la plus ancienne ; une ligne
         ouvre le `ScanDetail` de son code ; « Effacer l'historique » après confirmation ; un état vide
   - [x] La date du scan dans le `ScanDetail` (« Aujourd'hui à 21:48 »)
   - Vérifié sur l'émulateur Android et sur le simulateur iOS (tests d'interface, et jours plus
     anciens en anglais et en français sur une base préremplie)
   - L'écran du groupe passe au lot 9 : seul le scan multiple crée des captures de plusieurs codes,
     et c'est là qu'il se vérifie de bout en bout. D'ici là, une telle ligne ouvrirait son premier
     code.
9. **Scan multiple**
   - [x] Le mode, activé par un bouton des contrôles du scan : la détection ne s'arrête plus, chaque
         nouveau code est compté une fois (et fait vibrer selon le réglage), « Terminer » enregistre
         une seule capture ; l'arrêter avec des codes trouvés demande confirmation
   - [x] La feuille qui liste les codes trouvés (un appui ouvre le `ScanDetail` du code) ; un seul
         code trouvé s'affiche comme un scan simple
   - [x] L'écran du groupe, `Route.ScanGroup(scanId)` : ses codes, sa date, le partage (une valeur
         brute par ligne), la suppression ; ouvert depuis la feuille et depuis l'historique, où un
         groupe a sa propre icône
   - [x] La caméra remonte tous les codes d'une image, Android et iOS
   - Vérifié sur l'émulateur Android (codes ajoutés depuis des images) et sur le simulateur iOS
     (tests d'interface, caméra simulée qui lit plusieurs codes)
10. **Réglages, Feedback, À propos**
    - [x] `Route.Settings`, ouvert depuis le scan (roue dentée en haut à droite) : les trois réglages
          du scan, À propos, Feedback, noter l'app, la politique de confidentialité, Openium, la
          version et le build
    - [x] `Route.Feedback` : un sujet, un message, envoyés par l'app de mail (`mailto:` vers
          scanny.contact@gmail.com, le tag du sujet en objet)
    - [x] `Route.About` : logo, version, description, auteur, lien GitHub (retiré au lot 10 bis, son
          contenu est passé dans l'en-tête des réglages)
    - La politique de confidentialité de la v1 (`skylekayma.github.io`) répond 404 depuis le
      renommage du compte GitHub : l'app pointe vers `ludoskyle.github.io/scanny.github.io`. Le lien
      GitHub pointe vers `LudoSkyle/Scanny`, où l'ancien redirige.
    - Vérifié sur l'émulateur Android (Play Store, Gmail ouvert par le feedback) et sur le simulateur
      iOS (tests d'interface : le réglage du contenu brut jusque dans un détail, À propos, feedback,
      politique de confidentialité dans Safari, textes français)
10 bis. **Peaufinage avant la création**
    - [x] Feedback : le sujet se choisit dans un menu qui se déplie sous son champ
    - [x] Scan multiple : un code ne compte qu'une fois lu trois fois en une seconde, pour écarter
          les lectures erronées d'un code tenu devant la caméra
    - [x] Réglages redécoupés : un en-tête (logo, nom, version, description), puis « Préférences du
          scan » (vibration, bip, copie, ouverture du contenu), « Réglages de l'app » (contenu brut)
          et « Support » (étoile sur GitHub, feedback, noter l'app, politique
          de confidentialité, Openium) ; des lignes à icône ronde ; l'écran À propos est retiré, son
          contenu passe dans l'en-tête
    - Vérifié sur l'émulateur Android (écran, effacement de l'historique) et sur le simulateur iOS
      (tests d'interface : en-tête et sections, copie d'un code scanné collée dans le feedback,
      effacement de l'historique, textes français)
    - [x] Transitions entre les écrans : un glissement simple sur les deux plateformes, l'écran
          ouvert pousse l'autre sur toute la largeur, le geste retour joue le retour ;
          vers et depuis la caméra, un fondu de l'autre écran par-dessus elle, immobile (vérifié sur
          l'émulateur, animations ralenties : ouverture, retour, geste retour)
    - [x] Android : lecture de loin sans zoomer (le centre de l'image lu à part, une image sur deux)
          et aperçu plus net (16:9 en 1920×1080) ; sur un Galaxy S21 FE, une carte lue à plus de
          60 cm au lieu de 30, comme l'appareil photo de Samsung
    - [x] « Effacer l'historique » retiré des réglages : l'historique le propose déjà
    - [x] « Ouvrir les liens après un scan » devient « Ouvrir le contenu après un scan » : lien,
          e-mail, SMS, contact, réseau Wi-Fi
    - [x] Garde-fou : en scan simple, le même code est ignoré 1,5 s après la reprise de la détection
          (feuille fermée, retour du détail ou dans l'app) ; la caméra n'est pas écoutée quand une
          autre app ou un panneau du système est devant
    - [x] Historique : enregistrer ou non les scans, ignorer les doublons consécutifs (les mêmes
          codes dans les 5 minutes), durée de conservation (30 jours par défaut, ou toujours),
          purgée au démarrage ; la base passe en version 2 par une migration automatique
    - [x] Caméra avant ou arrière, en réglage (Android vérifié sur l'émulateur ; iOS à vérifier sur un
          iPhone)
11. **Création de QR codes et historique des créations** : l'éditeur et la personnalisation
    (couleurs, logo au centre, formes), et « Modifier » depuis le `ScanDetail` d'un code, qui ouvre
    l'éditeur prérempli avec son contenu. Décidé : la création s'ouvre par un « + » de la barre du
    scan ; l'historique bascule entre « Scans » et « Créations » sur le même écran ; le logo est une
    image de la galerie ; on crée un texte, un lien, un e-mail, un SMS, un réseau Wi-Fi, un contact,
    et des codes-barres.
    - [x] Le code d'un scan redessiné (qrose, qrose-oned) dans une boîte de dialogue, depuis la barre
          du `ScanDetail` ; pas de bouton pour un Data Matrix, un Aztec ou un PDF417, que qrose ne
          dessine pas
    - [x] Donnée et domaine des créations : table (base en version 3), encodage du contenu,
          enregistrement ou non dans l'historique (réglage, activé par défaut), durée de conservation
    - [x] L'éditeur : le « + » du scan, le type, les champs de chacun, l'aperçu en direct, la
          validation, l'enregistrement, et l'écran d'une création (son code, son contenu, sa
          suppression)
    - [x] Partager et enregistrer l'image d'un code créé
    - [x] La personnalisation : couleurs (une palette lisible) et formes, gardées avec la création
    - [x] Le logo au centre, depuis la galerie, réduit et gardé avec la création
    - [x] L'historique des créations : une seconde vue de l'historique, « Scans » ou « Créations »,
          chacune effacée à part
    - [x] « Modifier » depuis le `ScanDetail` : l'éditeur prérempli avec le contenu du code (un
          code-barres que l'éditeur dessine reste un code-barres), la création remplaçant l'éditeur
11 bis. **Retours après essai du lot 11** : la feuille d'un scan montre tout le contenu ; le code
    s'affiche dans une feuille, depuis le détail d'un scan comme d'une création, avec le partage et
    l'enregistrement de son image ; le contenu brut toujours accessible, replié, dans les deux détails
    (le réglage disparaît) ; dans l'historique, le type en pastille, les groupes mis en avant, et un
    glissement entre « Scans » et « Créations » ; une couleur libre en plus de la palette, avec un
    avertissement si le code risque d'être mal lu. Décidé : PNG d'abord, l'export en SVG viendra
    ensuite (qrose ne le fait pas : un dessin vectoriel à écrire, le plus coûteux pour les points
    arrondis reliés et le logo).
    - [x] La feuille d'un scan montre tous les champs du contenu (le message d'un SMS, le mot de
          passe d'un réseau…), un long texte coupé à quatre lignes
    - [x] Le code dans une feuille, depuis les deux détails, avec le partage et l'enregistrement de
          son image (un code scanné aussi)
    - [x] Le contenu brut replié dans les deux détails, sans réglage (pas pour un texte ou un lien,
          qui sont leur propre contenu brut)
    - [x] L'historique : le type en pastille, les groupes mis en avant (badge et pastille remplis), le
          glissement entre les vues
    - [x] Une couleur libre, avec un avertissement de lisibilité (contraste trop faible ou code clair
          sur fond sombre), qui n'empêche pas de créer
    - [ ] L'export en SVG *(décidé, non engagé)*
11 ter. **Deuxièmes retours** : l'éditeur en deux étapes ; modifier une création ; un compteur de la
    taille du contenu ; une couleur saisie en hexadécimal ou en RVB ; une snackbar « Voir » après
    l'enregistrement d'une image ; le contenu brut plus visible replié, et un réglage pour l'afficher
    dans la feuille d'un scan ; la ligne et le détail d'un groupe ; une fiche « i » sur le type et le
    format d'un code. Décidé : modifier une création en crée une nouvelle, comme pour un code scanné ;
    pas de notification après l'enregistrement d'une image ; un groupe montre « Groupe de 3 codes »
    et les types de ses codes ; « Étape 1 sur 2 » en sous-titre. Ajouté en cours de route : des
    options avancées. Le niveau de correction va dans celles de l'étape 1, là où le compteur montre
    ce qu'il change : la limite du contenu le suit (jusqu'à 2953 octets en correction faible), et un
    logo, qui demande la correction maximale, se désactive avec sa raison quand le niveau ou le contenu
    ne le permettent pas. La couleur des coins, un dégradé, la forme et la marge du logo vont dans
    celles de l'étape 2. Écartés : la forme ronde du code et le quatrième œil, hors norme, qui
    gênent la lecture.
    - [x] L'éditeur en deux étapes : le contenu et son aperçu, puis la personnalisation ; le retour,
          de la barre comme du système, ramène au contenu sans perdre la personnalisation, perdue en
          quittant l'éditeur ; un code-barres n'a qu'une étape
    - [x] Modifier une création : son contenu et son style dans l'éditeur, un nouveau code au bout
    - [x] Le compteur de la taille du contenu : ce que le contenu entier prend du QR code, rouge
          au-delà de ce qu'il tient
    - [x] Le niveau de correction, dans les options avancées du contenu : la limite et le compteur
          le suivent ; un logo n'est possible qu'en correction automatique ou maximale
    - [x] Les options avancées de la personnalisation : la couleur des coins
    - [x] Un dégradé, dans les options avancées de la personnalisation
    - [x] La forme et la marge du logo, dans les options avancées de la personnalisation
    - [x] Une couleur saisie en hexadécimal ou en RVB
    - [x] La snackbar « Voir » après l'enregistrement d'une image
    - [x] Le contenu brut plus visible replié, et son réglage pour la feuille d'un scan
    - [x] La ligne d'un groupe dans l'historique, et la séparation dans son détail
    - [x] Les fiches « i » du format, juste à côté de lui : son usage, comment il est formé, et un
          exemple décortiqué
    - [x] Une FAQ des formats de codes dans les réglages, avec les textes de ces fiches
    - [x] Réinitialiser la personnalisation depuis sa barre du haut, avec une annulation
    - [x] L'avertissement de lisibilité calé sur ce que ZXing lit, plutôt que sur le contraste d'un
          texte
12. **Finitions** : splash, README à jour (il cite encore Hilt et Camera 2), CI. Deux remarques
    d'ergonomie relevées au lot 6 : le message « aucun code trouvé » masque le bouton galerie le temps
    de son affichage, et le Photo Picker range les images de la plus récente à la plus ancienne.
13. **Favoris et annotations** *(pas urgent)* : sur une capture, depuis l'historique et l'écran du
    groupe.

## Décisions prises

- **Projet Xcode** : généré par XcodeGen (`iosApp/project.yml`), jamais édité à la main ; le projet
  généré est commité.
- **Identité iOS** : bundle ID `fr.skyle.scanny`, comme sur Android. Cible minimale iOS 18.5 : les
  mêmes iPhone que les iOS 17 et 18 (XS/XR et plus récents) ; en dessous, l'éditeur de liens avertit
  qu'un objet embarqué par Compose Multiplatform est compilé pour 18.5. Pas encore de compte Apple : le
  développement se fait sur simulateur, un Apple ID gratuit suffit pour un iPhone, le programme
  payant n'est nécessaire que pour TestFlight, l'App Store et le WiFi.
- **Tests d'écran** : en Compose (`runComposeUiTest`), sur la JVM et le simulateur iOS ; XCUITest
  ne garde que les parcours qui passent par le système, en parallèle
  ([ADR-0003](./adr/0003-les-ecrans-se-testent-en-compose.md)).
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
- **Après un scan, une feuille rapide** reste par-dessus la caméra : un code y montre son type, tout
  son contenu (chaque champ, un long texte coupé à quatre lignes) et son action principale ; plusieurs codes (scan multiple) y sont listés. « Voir le détail »
  et un appui sur un code ouvrent l'écran `Route.ScanDetail(codeId)`, celui d'**un** code, partagé avec
  l'historique.
- **« Modifier » un code scanné**, c'est reprendre son contenu dans l'éditeur de création, le
  modifier, le personnaliser (couleurs, logo au centre…) et générer le nouveau QR code. Un code
  s'édite seul, jamais un groupe. Le scan, lui, reste tel qu'il a été lu : le QR code généré est une
  création, rangée dans l'historique des créations.
- **Favori et annotation** portent sur une capture : un scan simple, ou un groupe entier. Pas urgent
  (lot 13) ; qu'un code d'un groupe puisse avoir les siens se décidera avec le lot.

## Décisions ouvertes

- **CI** : le build iOS demande un exécuteur macOS sur CircleCI.