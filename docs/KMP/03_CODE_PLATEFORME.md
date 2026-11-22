## Code propre à chaque plateforme

Toute l'UI est commune (voir [ADR-0002](../adr/0002-ui-commune-en-compose-multiplatform.md)). Ce
document dit où et comment écrire ce qui ne peut pas l'être : la caméra et la détection, le choix
d'une image, les permissions, les actions système.

### Avant d'écrire du code de plateforme

Chercher d'abord l'API commune :

- l'heure courante passe par `kotlin.time.Clock`, les dates et leurs formats par kotlinx-datetime,
  jamais par `java.util` ou `NSDate` ;
- les fichiers passent par kotlinx-io ;
- ouvrir une URL (`https:`, `mailto:`, `sms:`, `tel:`) passe par `LocalUriHandler`, faire vibrer par
  `LocalHapticFeedback`, copier par `LocalClipboard` : ce sont des API de Compose, communes. Pour le
  presse-papiers, la création du `ClipEntry` texte passe par une fonction `expect` si l'API commune
  ne la fournit pas.

### Trois façons d'écrire du natif

| Le besoin… | Se déclare… | Utilisé par |
|---|---|---|
| passe par une API de Compose (lien, vibration, presse-papiers) | rien à déclarer : `LocalUriHandler`, `LocalHapticFeedback`, `LocalClipboard` | la `Route`, en réaction à un clic ou à un event du ViewModel |
| est une action système sans lien avec la composition (partager, ajouter un contact, noter l'app) | une **interface** en `commonMain`, une implémentation par plateforme, fournies par le module Koin de plateforme | le ViewModel, qui la reçoit par constructeur (et un fake dans ses tests) |
| a besoin de la composition (aperçu caméra, demande de permission, sélecteur d'image, rejoindre un WiFi) | une fonction `@Composable` `expect`, avec un `actual` par plateforme | l'écran, via ses composants, ou la `Route` |

- Un `expect` porte sur une fonction ou une propriété, jamais sur une classe : les classes
  `expect`/`actual` sont encore en bêta. Un état natif à exposer se décrit par une interface
  commune, rendue par une fonction `expect` (`rememberCameraPermissionState(): CameraPermissionState`).
- Le code de plateforme vit dans `shared`, sous `platform/<capacité>/` (`platform/camera/`,
  `platform/share/`…), dans le source set de sa plateforme. Le fichier d'un `actual` est suffixé par
  sa plateforme : `ScanCamera.android.kt`, `ScanCamera.ios.kt`.
- Le module Koin de plateforme (`internal expect val platformSharedModule: Module`) déclare les
  implémentations natives ; `sharedModule` l'inclut.

Exemple d'action système :

`commonMain/.../platform/share/TextSharer.kt`

```kotlin
interface TextSharer {

    fun shareText(text: String)
}
```

`androidMain/.../platform/share/TextSharer.android.kt`

```kotlin
internal class AndroidTextSharer(
    private val context: Context,
) : TextSharer {

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(
            Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
```

`iosMain/.../platform/share/TextSharer.ios.kt`

```kotlin
internal class IosTextSharer : TextSharer {

    override fun shareText(text: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

        topViewController()?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }
}
```

### Règles par plateforme

Android :

- Une action système part du `Context` de l'application, avec `FLAG_ACTIVITY_NEW_TASK` : une
  implémentation ne garde jamais de référence à une `Activity`.
- Ce qui attend un résultat d'activité (permission, sélecteur d'image) passe par
  `rememberLauncherForActivityResult`, donc par un `expect` composable.

iOS :

- Le contrôleur depuis lequel présenter une feuille (partage, contact, événement) se cherche **au
  moment de l'action** (`topViewController()`), jamais pendant la composition : la composition ne
  dépend pas de son hôte.
- Les vues natives (aperçu caméra `AVCaptureVideoPreviewLayer`) s'intègrent par `UIKitView`.
- Les appels UIKit se font sur le thread principal ; le démarrage et l'arrêt d'une
  `AVCaptureSession` se font hors du thread principal.
- Un `@OptIn(ExperimentalForeignApi::class)` se place au plus près de l'appel qui l'exige, pas sur
  un fichier entier.
- Toute API qui demande une autorisation déclare sa clé d'usage dans l'`Info.plist` de `iosApp`
  (`NSCameraUsageDescription` pour la caméra, `NSPhotoLibraryAddUsageDescription` pour enregistrer un
  QR code dans Photos…), traduite en anglais et en français.

### Caméra et détection

Tout vit dans `shared`, sous `platform/camera/` et `platform/gallery/` :

| Déclaration commune | Rôle | Android | iOS |
|---|---|---|---|
| `ScanCamera(...)` | Aperçu de la caméra choisie (arrière, ou avant en réglage) et détection en direct | CameraX (`CameraXViewfinder`) + ML Kit (`BarcodeAnalyzer`) | `AVCaptureSession`, `AVCaptureVideoPreviewLayer` dans une `UIView` (`UIKitView`), `AVCaptureMetadataOutput` |
| `rememberCameraPermissionState()` | Statut de la permission, la demander, ouvrir les réglages de l'app | `RequestPermission`, `shouldShowRequestPermissionRationale` | `AVCaptureDevice.requestAccess`, `UIApplicationOpenSettingsURLString` |
| `rememberImageBarcodeScanner(...)` | Choisir une image et y lire un code | Photo Picker + ML Kit | `PHPickerViewController` + Vision |
| `rememberLogoPicker(...)` | Choisir une image pour le logo d'un code, réduite | Photo Picker + `ImageDecoder` (`BitmapFactory` avant Android 9) | `PHPickerViewController` + `UIGraphicsImageRenderer` |

- La caméra se choisit par `CameraLens` (`BACK` par défaut, `FRONT` quand le réglage « Utiliser la
  caméra avant » le demande) : `CameraSelector.DEFAULT_FRONT_CAMERA` sur Android,
  `AVCaptureDevicePositionFront` sur iOS. Une autre caméra rouvre la caméra ; ses capacités (torche,
  zoom) remontent de nouveau. La caméra simulée du simulateur iOS l'ignore.
- `ScanCamera` est piloté par l'état de l'écran : détection active ou en pause, torche, niveau de
  zoom, demande de mise au point (`FocusRequest`, un point en fraction de l'aperçu et un id qui
  distingue deux appuis au même endroit). Il remonte ce que seul l'appareil connaît
  (`CameraCapabilities` : torche, plage de zoom) et l'absence de caméra (le simulateur iOS).
- Chaque code vu dans une image remonte, l'un après l'autre, sous la forme commune `DetectedBarcode`
  (texte brut et `BarcodeFormat`, un modèle de `domain`), jamais sous un type de ML Kit,
  d'AVFoundation ou de Vision. Un scan simple garde le premier et se met en pause ; un scan multiple
  les garde tous, chacun une fois. Sa normalisation,
  qui en fait le même code sur les deux plateformes (un EAN-13 qui commence par `0` est un UPC-A), est
  appliquée par `RecordScanUseCase` à tout code enregistré : le code de plateforme et le ViewModel
  le transmettent tel que lu.
- En scan simple, la détection est mise en pause par le ViewModel **avant** tout traitement, de façon
  synchrone : la caméra lit le même code plusieurs fois de suite, et chaque lecture serait sinon
  enregistrée. En scan multiple, elle ne s'arrête pas : le ViewModel ignore un code déjà compté, et
  la pause ne vient qu'avec l'enregistrement, à « Terminer ».
- En scan multiple, un code ne compte qu'une fois lu de façon stable : 3 lectures en moins d'une
  seconde. Un code tenu devant la caméra est lu bien plus souvent ; une lecture parasite (un code-barres
  décodé à tort dans le motif d'un QR code, un reflet) l'est une fois, avec une valeur qui change
  d'une image à l'autre. Un code trouvé dans une image de la galerie, lu une seule fois et à dessein,
  compte tout de suite. La caméra simulée du simulateur iOS lit donc chacun de ses codes plusieurs
  fois.
- Elle reste en pause quand la feuille du résultat ouvre le détail du code : la caméra lit encore
  pendant la transition vers l'écran suivant. Elle reprend au retour sur le scan, quand l'écran
  repasse au premier plan (`onScreenResumed`).
- En scan simple, le même code relu juste après une reprise de la détection (feuille fermée, retour
  du détail ou dans l'app, fin de la pause qui suit l'ouverture du contenu) est ignoré pendant 1,5 s
  (`SAME_CODE_PAUSE`) : encore devant la caméra, il rouvrirait son résultat dès qu'on le ferme. Un
  autre code, ou une image de la galerie, est lu tout de suite.
- Quand une autre app ou un panneau du système passe devant (navigateur, Contacts, ajout d'un
  réseau), l'écran n'est plus `RESUMED` et ce que la caméra voit encore est ignoré : la `Route` le
  signale depuis un `LifecycleResumeEffect` (`onScreenPaused`, `onScreenResumed`). Sans quoi un
  contenu ouvert automatiquement se rouvrirait en boucle, le code étant toujours devant la caméra.
- Le geste de zoom (pincement, plafonné à ×8) et le geste de mise au point (appui) sont détectés en
  commun, dans `ScanCameraLayer` ; seule leur application à la caméra est native.
- Android, lecture de loin : ML Kit réduit lui-même une image avant d'y chercher des codes, et une
  image plus fine ne lit pas plus loin (constaté sur un Galaxy S21 FE : la même carte lue à 30 cm en
  960×720, en 1440×1080 comme en 4000×3000). Une image sur deux, `BarcodeAnalyzer` ne lui donne que
  le centre, la moitié du petit côté (le cadre de l'écran avec une marge), découpé dans une image
  1920×1440 : un code lointain y est lu comme si la caméra zoomait, sans que l'aperçu zoome (la même
  carte lue à plus de 60 cm). L'autre image part entière, pour un code tenu de près, plus large que
  le centre. `MlKitAnalyzer` de CameraX ne sait pas donner une partie de l'image à ML Kit :
  `BarcodeAnalyzer` est un `ImageAnalysis.Analyzer`, qui ferme chaque image dans tous les cas, sans
  quoi CameraX n'envoie plus les suivantes.
- Android, aperçu : il remplit l'écran, son image est donc coupée sur les côtés et agrandie. Le 4:3
  par défaut de CameraX (1440×1080) l'est d'environ 1,6 fois sur un téléphone, et paraît flou ;
  l'aperçu demande donc du 16:9 en 1920×1080, agrandi d'environ 1,2 fois. L'analyse reste en 4:3, le
  champ entier de la caméra, plus large que l'aperçu : tout code à l'écran est lu. Les deux tailles
  forment une paire que tout appareil sait produire en même temps.
- La permission a quatre statuts : `UNKNOWN` (pas encore demandée, ou réponse attendue), `GRANTED`,
  `DENIED` (Android laisse encore la redemander) et `DENIED_FOREVER` (seuls les réglages peuvent
  l'accorder : après deux refus sur Android, dès le premier sur iOS). L'écran la demande à son
  ouverture quand elle est `UNKNOWN`, et le statut est relu à chaque retour au premier plan.
- Android : la permission `CAMERA` est déclarée dans le manifeste de `shared/src/androidMain`, fusionné
  dans celui de l'app. Sans elle, Android refuse la demande sans rien afficher.
- iOS : `NSCameraUsageDescription` est dans l'`Info.plist`, traduite dans `en.lproj` et `fr.lproj`.
- Le scan d'une image ne demande aucune permission : il reste proposé quand la caméra est refusée ou
  absente.
- **Sur le simulateur iOS, la détection ne se vérifie pas** : il n'a pas de caméra, et Vision n'y
  détecte aucun code dans une image (limite connue du simulateur, quelle que soit la révision de la
  requête). La détection iOS se vérifie sur un iPhone. Pour tester ce qui suit un scan, un build
  Debug sur simulateur remplace la caméra par `SimulatedScanCamera`, qui lit le ou les codes donnés
  dans l'environnement de l'app (`SimulatedScan`, voir [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md)).

### Actions sur un code

- Ouvrir un lien, écrire un e-mail ou un SMS passe par `LocalUriHandler`, depuis la `Route`. Le lien
  vient du domaine (`mailto:`, `sms:` de la RFC 5724) ; `platformSmsLink` l'adapte à la plateforme :
  Android l'ouvre tel quel, iOS reçoit `&body=` au lieu de `?body=`. Messages d'iOS 26 lit les deux
  (constaté sur le simulateur) ; `&body=` est gardé, la forme que lisaient les versions plus
  anciennes. Quand aucune app ne l'ouvre, Android lève une exception : l'échec est journalisé, et
  l'écran le dit. Le `LocalUriHandler` d'iOS, lui, ne signale aucun échec : sans app pour le lien
  (Mail absent), rien ne se passe.
- Appeler un numéro et voir une position sur la carte passent par le même chemin. Le lien `tel:`
  vient du domaine ; Android ouvre le clavier du téléphone, numéro composé, sans appeler : aucune
  permission n'est demandée. Une position s'ouvre par `platformMapsLink` : le lien `geo:` sur
  Android (Google Maps l'épingle sous son nom), le lien de Plans (`https://maps.apple.com/?ll=…&q=…`)
  sur iOS, qui n'a pas d'app pour un `geo:`. Un marque-page ouvre son lien.
- La feuille du résultat a ses propres messages (`resultSnackbarState` de la `Route` du scan), au
  bas de la feuille : ceux de l'écran du scan seraient cachés dessous. Ce qui suit une action, un
  lien sans app, un contact qui ne s'ajoute pas, un réseau Wi-Fi, va à la feuille quand elle est
  ouverte, à l'écran sinon (ouverture automatique du contenu).
- Copier passe par `LocalClipboard` et `textClipEntry(text)`, un `expect` : l'API commune de Compose
  ne sait pas construire l'entrée d'un texte. Depuis Android 13, le système montre lui-même ce qui a
  été copié : l'app ne le redit pas (`isCopyConfirmedBySystem`). Sous la feuille du résultat, où un
  message de l'écran serait caché, c'est le bouton qui affiche « Copié ».
- Le réglage « Ouvrir le contenu après un scan » lance, dès le scan et sans la feuille, l'action du
  bouton principal du résultat (`toCodeAction`), par le même chemin dans la `Route` : un lien, un
  e-mail, un SMS, un contact, un réseau Wi-Fi que l'appareil sait rejoindre. Un texte, ou un réseau
  qu'il ne sait pas rejoindre, s'affiche dans la feuille. La détection reprend 2 s plus tard.
- Le « i » à côté du format, dans l'en-tête d'un détail (`CodeHeader`), ouvre la fiche de ce format
  (`CodeInfoSheet`) : son usage, comment il est formé, et un code de ce format décortiqué (un
  EAN-13 en préfixe du pays, entreprise, produit et chiffre de contrôle, par exemple). Le format et
  son « i » se touchent ensemble, pour une cible plus grande que l'icône. Les chiffres de contrôle
  des exemples sont exacts.
- Le code se montre dans une feuille (`CodeImageSheet`), ouverte par « Afficher le code » du détail
  d'un scan comme de celui d'une création, en grand pour qu'un autre téléphone le scanne, avec les
  actions sur son image : la partager, l'enregistrer dans les photos (`CodeImageExporter`, qui la
  dessine puis la confie à `ImageSharer` ou `ImageSaver`). Enregistrée, un message le dit dans la
  feuille, avec « Voir » pour l'ouvrir dans les photos : un message de l'écran passerait sous elle ;
  un échec la ferme, pour que le message se lise. Un code scanné y est redessiné sombre sur blanc, une création dans son style.
- L'image d'un code se dessine en commun (`CodeImageRenderer`, hors du fil principal) : un PNG
  de 1024 pixels de côté pour un QR code, dans son style, 1200 × 480 pour un code-barres, sombre sur
  blanc, avec la marge dont un lecteur a besoin autour d'un code, de la couleur du fond.
- Le logo d'un QR code est une image de la galerie, réduite dès qu'elle est choisie : redressée, en
  PNG, de 256 pixels de côté au plus (`logoSize`, en commun), assez pour le cinquième d'une image de
  1024 pixels qu'il occupe, et assez léger pour être gardé avec la création. Android la lit par
  `ImageDecoder`, qui redresse une photo selon son EXIF et lit le HEIF ; avant Android 9,
  `BitmapFactory` la lit à une fraction de sa taille, sans lire son EXIF. iOS la redessine par
  `UIGraphicsImageRenderer`, qui la redresse ; le sélecteur de photos (`ImagePickerDelegate`,
  `presentImagePicker`) sert aussi au scan d'une image. Une image illisible est dite par un message.
- qrose dessine le logo au centre et efface les points sous lui et autour, pour que le fond y
  paraisse. Un code avec logo est encodé à la correction la plus forte, fixée par Scanny : qrose
  garderait la plus faible pour un logo posé sans marge. Sans logo, il l'est à la correction
  choisie. Le texte est jugé à cette même correction avant d'être dessiné (`qrErrorCorrection`).
- La partager passe par `ImageSharer`. Android écrit le PNG dans le cache (`shared_images/`), que le
  `FileProvider` de l'app (`${applicationId}.files`, déclaré dans le manifeste de `shared`) prête à
  l'app choisie dans la feuille de partage. iOS partage une `UIImage` plutôt qu'un fichier : la
  feuille propose alors de la garder dans les photos.
- L'enregistrer passe par `ImageSaver`. Android l'écrit par `MediaStore` dans `Pictures/Scanny`,
  sans permission à partir d'Android 10 ; en dessous, l'action n'est pas proposée (`canSave`), la
  feuille de partage gardant l'image ailleurs. iOS l'ajoute aux photos par `PHPhotoLibrary`, en accès
  ajout seul : la clé `NSPhotoLibraryAddUsageDescription` de l'`Info.plist` dit pourquoi, en anglais
  et en français. L'image enregistrée revient en `SavedImage` : son URI de contenu sur Android,
  l'identifiant local de son asset sur iOS.
- La voir passe par `rememberSavedImageOpener`. Android ouvre l'image elle-même dans la galerie
  (`ACTION_VIEW`, avec le droit de la lire). iOS ne laisse aucune app ouvrir une photo donnée : il
  ouvre l'app Photos (`photos-redirect://`), où l'image est la plus récente.
- Partager passe par `TextSharer`, une interface, et partage la valeur brute du code : c'est ce
  qu'une autre app, ou un autre scanner, sait relire. Le module Koin de plateforme
  (`platformSharedModule`) fournit son implémentation.
- Ajouter un contact passe par `ContactAdder`, une interface appelée par le ViewModel. Android ouvre
  l'app Contacts (`ACTION_INSERT_OR_EDIT`), qui laisse créer un contact ou compléter un contact
  existant ; téléphones, e-mails, adresses et sites passent en lignes de données
  (`Insert.DATA`), sans la limite de trois des extras `PHONE` ou `EMAIL`. iOS présente
  `CNContactViewController.viewControllerForNewContact` dans un `UINavigationController`, pour ses
  boutons Annuler et OK ; son délégué ferme l'écran, et l'adder le garde, car le contrôleur ne le
  retient pas. Cet écran ne demande aucune autorisation d'accès aux contacts, c'est lui qui
  enregistre (constaté sur le simulateur : contact enregistré avec tous ses champs, sans
  `NSContactsUsageDescription`).
- Rejoindre un WiFi est en deux morceaux. `WifiNetworkSupport` (interface, ViewModel) dit si
  l'appareil peut rejoindre le réseau ; l'action n'est proposée que dans ce cas : toujours sur
  Android, jamais sur iOS tant qu'il manque l'entitlement *Hotspot Configuration*.
  `rememberWifiNetworkJoiner()` (`expect` composable, appelé par la `Route`) fait ce que le système
  permet à une app, et dit ce qui en est sorti (`WifiJoinResult`) :
  - À partir d'Android 11, pour un réseau ouvert ou un WPA dont le système accepte le mot de passe
    (8 à 63 caractères ASCII imprimables), l'écran du système qui enregistre un réseau
    (`ACTION_WIFI_ADD_NETWORKS`). Il ne s'affiche qu'à une app qui attend son résultat, d'où
    `rememberLauncherForActivityResult`. Son résultat est une `ArrayList<Integer>`
    (`getIntegerArrayListExtra`, pas `getIntArrayExtra`) : enregistré, déjà enregistré (l'écran se
    ferme aussitôt, sans rien montrer), refusé ; sans résultat, l'utilisateur a annulé.
  - Sinon (avant Android 11, où `WifiManager.addNetwork` échoue toujours pour une app qui vise
    Android 10 et plus ; un WEP ; un mot de passe que l'écran refuserait), le mot de passe est copié,
    marqué sensible, et les réglages Wi-Fi s'ouvrent : le panneau par-dessus l'app à partir
    d'Android 10, l'écran des réglages avant.
  - Le message suit : « enregistré », ou « déjà enregistré » et « refusé » avec une action qui ouvre
    les réglages Wi-Fi. Rien pour une annulation. Quand les réglages s'ouvrent, un message de l'app
    serait caché dessous : la consigne (« Mot de passe copié : choisissez « … » dans la liste »)
    passe par un `Toast`, le seul message d'une app que le système dessine par-dessus une autre.

### Signaler un scan

- La vibration passe par `LocalHapticFeedback` (`Confirm`), dans la `Route`, sur l'event `Vibrate`
  du ViewModel.
- Le bip passe par `ScanBeeper`, une interface appelée par le ViewModel. Android joue
  `ToneGenerator.TONE_PROP_BEEP` sur le flux des notifications, dont le volume et le mode silencieux
  s'appliquent ; un `ToneGenerator` garde une piste audio tant qu'il vit, il est libéré après le bip.
  iOS joue le son système 1057, muet quand le téléphone est en silencieux. Un test ne l'entend pas :
  le bip s'écoute sur un appareil.
- La copie du code après un scan est un event du ViewModel, `CopyToClipboard`, que la `Route` copie
  comme une partie du détail, sans message : la feuille du résultat s'ouvre au même moment et le
  cacherait.

### Clavier d'une coordonnée

Une latitude ou une longitude se tape avec `coordinateKeyboardType`, un `expect` : il faut des
chiffres, un séparateur décimal et un signe moins (une coordonnée au sud ou à l'ouest). Le pavé
décimal d'Android les a (`KeyboardType.Decimal`, vérifié avec Gboard sur l'émulateur). Celui d'iOS
(`UIKeyboardTypeDecimalPad`, où Compose envoie `Decimal`) n'a pas de moins : iOS prend le clavier
ASCII, dont la page des chiffres a les deux. La virgule et le point sont acceptés comme séparateur.

### Couleur des icônes des barres système

Elle suit le style de `SCScreenStructure` : clair sur le style `BLACK`, sombre sinon. Android règle
les icônes de la fenêtre (`WindowCompat`). iOS n'a rien à faire pour l'instant : sur l'écran noir,
ses icônes ont été vues claires sur le simulateur ; à confirmer sur un iPhone, et au premier écran sur
fond clair.

### Matrice des capacités

Mécanisme visé pour chaque capacité. Une ligne est confirmée (✅) quand son implémentation est
livrée ; le tableau est mis à jour dans le même commit.

| Capacité | Android | iOS | Forme |
|---|---|---|---|
| Aperçu et détection en direct | ✅ (Galaxy S21 FE, Android 16 : aperçu, détection de près et de loin ; émulateur : aperçu) — CameraX + ML Kit Barcode Scanning | Livré, à vérifier sur un iPhone — `AVCaptureSession` + `AVCaptureMetadataOutput` | `expect` composable |
| Zoom, mise au point | ✅ zoom (Galaxy S21 FE) ; mise au point à vérifier sur un appareil — `CameraControl` (`setZoomRatio`, `startFocusAndMetering`) | Livré, à vérifier sur un iPhone — `AVCaptureDevice` (`videoZoomFactor`, `focusPointOfInterest`) | `expect` composable |
| Caméra avant | ✅ (émulateur : caméra avant émulée) — `CameraSelector.DEFAULT_FRONT_CAMERA` | Livré, à vérifier sur un iPhone — `AVCaptureDevicePositionFront` | `expect` composable (`CameraLens`) |
| Torche | Livré, à vérifier sur un appareil (l'émulateur n'a pas de flash) — `CameraControl.enableTorch` | Livré, à vérifier sur un iPhone — `AVCaptureDevice.torchMode` | `expect` composable |
| Permission caméra | ✅ (refus, refus définitif, accord) — `RequestPermission` | ✅ demande et description affichées (simulateur) — `AVCaptureDevice.requestAccess` | `expect` composable |
| Détection dans une image | ✅ (émulateur : code lu, enregistré ; image sans code signalée) — Photo Picker + ML Kit (`InputImage`) | Livré, à vérifier sur un iPhone — `PHPickerViewController` + Vision (`VNDetectBarcodesRequest`) | `expect` composable |
| Ouvrir un lien, un mail, un SMS | ✅ (émulateur : lien ouvert dans Chrome, SMS prérempli dans Messages) — `LocalUriHandler` | ✅ lien et SMS (simulateur : Safari, message prérempli dans Messages) ; mail à vérifier sur un iPhone (pas de Mail sur le simulateur) — `LocalUriHandler`, `sms:` avec `&body=` | Compose |
| Appeler un numéro | ✅ (émulateur : clavier du téléphone, numéro composé) — `tel:` par `LocalUriHandler` | À vérifier sur un iPhone (pas d'app Téléphone sur le simulateur) — `tel:` par `LocalUriHandler` | Compose |
| Voir une position sur la carte | ✅ (émulateur : Google Maps, lieu épinglé sous son nom) — `geo:…?q=lat,lon(nom)` | ✅ (simulateur : Plans ouvert) — `https://maps.apple.com/?ll=…&q=…` | Compose + `expect` pour le lien (`platformMapsLink`) |
| Vibration | `LocalHapticFeedback` (`Confirm`) | `LocalHapticFeedback` (`Confirm`) | Compose |
| Bip après un scan | Livré, à écouter sur un appareil (l'émulateur tourne sans son) — `ToneGenerator` (`TONE_PROP_BEEP`, flux des notifications) | Livré, à écouter sur un iPhone — `AudioServicesPlaySystemSound` (son système 1057) | interface (`ScanBeeper`) |
| Copier | ✅ (émulateur : confirmation du système, Android 13 et plus) — `LocalClipboard`, `ClipData` | ✅ (simulateur : texte dans le presse-papiers, message de l'app ; copie après un scan collée dans un champ de l'app) — `LocalClipboard`, `ClipEntry.withPlainText` | Compose + `expect` pour créer le `ClipEntry` |
| Partager un texte | ✅ (émulateur : feuille de partage) — `ACTION_SEND` | ✅ (simulateur : feuille de partage) — `UIActivityViewController` | interface |
| Partager, enregistrer un QR code | ✅ (émulateur : feuille de partage, image dans `Pictures/Scanny`, relue) — `FileProvider` + `ACTION_SEND`, `MediaStore`, Android 10 et plus | ✅ (simulateur : feuille de partage, image ajoutée aux photos, relue) — `UIActivityViewController`, `PHPhotoLibrary` | interfaces (`ImageSharer`, `ImageSaver`) |
| Ajouter un contact | ✅ (émulateur : Contacts prérempli, tous les champs) — `ACTION_INSERT_OR_EDIT` | ✅ (simulateur : contact enregistré, tous les champs) — `CNContactViewController` | interface |
| Rejoindre un réseau WiFi | ✅ (émulateurs Android 9, 10, 11 et 17 : réseau enregistré, déjà enregistré, annulé ; réglages et consigne avant Android 11) — `Settings.ACTION_WIFI_ADD_NETWORKS` à partir d'Android 11, sinon panneau ou réglages Wi-Fi et mot de passe copié | Reporté (`NEHotspotConfigurationManager` exige l'entitlement *Hotspot Configuration*, donc un compte payant) : action non proposée | `expect` composable + interface |
| Ouvrir les réglages de l'app | ✅ `ACTION_APPLICATION_DETAILS_SETTINGS` | `UIApplicationOpenSettingsURLString` | dans l'état de permission caméra |
| Noter l'app | ✅ (émulateur : Play Store) — `market://`, sinon le site du Play Store | Pas encore sur l'App Store : l'entrée n'est pas proposée | `expect val rateAppLinks` |
| Version de l'app | ✅ — `PackageManager` (`versionName`, `longVersionCode`) | ✅ (simulateur) — `CFBundleShortVersionString`, `CFBundleVersion` | interface (`AppVersion`) |