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
| `ScanCamera(...)` | Aperçu de la caméra arrière et détection en direct | CameraX (`CameraXViewfinder`) + ML Kit (`MlKitAnalyzer`) | `AVCaptureSession`, `AVCaptureVideoPreviewLayer` dans une `UIView` (`UIKitView`), `AVCaptureMetadataOutput` |
| `rememberCameraPermissionState()` | Statut de la permission, la demander, ouvrir les réglages de l'app | `RequestPermission`, `shouldShowRequestPermissionRationale` | `AVCaptureDevice.requestAccess`, `UIApplicationOpenSettingsURLString` |
| `rememberImageBarcodeScanner(...)` | Choisir une image et y lire un code | Photo Picker + ML Kit | `PHPickerViewController` + Vision |

- `ScanCamera` est piloté par l'état de l'écran : détection active ou en pause, torche, niveau de
  zoom, demande de mise au point (`FocusRequest`, un point en fraction de l'aperçu et un id qui
  distingue deux appuis au même endroit). Il remonte ce que seul l'appareil connaît
  (`CameraCapabilities` : torche, plage de zoom) et l'absence de caméra (le simulateur iOS).
- Un code détecté remonte sous la forme commune `DetectedBarcode` (texte brut et `BarcodeFormat`, un
  modèle de `domain`), jamais sous un type de ML Kit, d'AVFoundation ou de Vision. Sa normalisation,
  qui en fait le même code sur les deux plateformes (un EAN-13 qui commence par `0` est un UPC-A), est
  appliquée par `RecordScanUseCase` à tout code enregistré : le code de plateforme et le ViewModel
  le transmettent tel que lu.
- La détection est mise en pause par le ViewModel **avant** tout traitement, de façon synchrone : la
  caméra lit le même code plusieurs fois de suite, et chaque lecture serait sinon enregistrée.
- Elle reste en pause quand la feuille du résultat ouvre le détail du code : la caméra lit encore
  pendant la transition vers l'écran suivant. Elle reprend au retour sur le scan, que la `Route`
  signale en se composant de nouveau (`onScreenShown`).
- Le geste de zoom (pincement, plafonné à ×8) et le geste de mise au point (appui) sont détectés en
  commun, dans `ScanCameraLayer` ; seule leur application à la caméra est native.
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
  Debug sur simulateur remplace la caméra par `SimulatedScanCamera`, qui lit le code donné dans
  l'environnement de l'app (`SimulatedScan`, voir [iosApp/CLAUDE.md](../../iosApp/CLAUDE.md)).

### Actions sur un code

- Ouvrir un lien, écrire un e-mail ou un SMS passe par `LocalUriHandler`, depuis la `Route`. Le lien
  vient du domaine (`mailto:`, `sms:` de la RFC 5724) ; `platformSmsLink` l'adapte à la plateforme :
  Android l'ouvre tel quel, iOS reçoit `&body=` au lieu de `?body=`. Messages d'iOS 26 lit les deux
  (constaté sur le simulateur) ; `&body=` est gardé, la forme que lisaient les versions plus
  anciennes. Quand aucune app ne l'ouvre, Android lève une exception : l'échec est journalisé, et
  l'écran du détail le dit. Le `LocalUriHandler` d'iOS, lui, ne signale aucun échec : sans app pour
  le lien (Mail absent), rien ne se passe.
- Copier passe par `LocalClipboard` et `textClipEntry(text)`, un `expect` : l'API commune de Compose
  ne sait pas construire l'entrée d'un texte. Depuis Android 13, le système montre lui-même ce qui a
  été copié : l'app ne le redit pas (`isCopyConfirmedBySystem`). Sous la feuille du résultat, où un
  message de l'écran serait caché, c'est le bouton qui affiche « Copié ».
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
  l'appareil peut rejoindre le réseau ; l'action n'est proposée que dans ce cas. Android : à partir
  d'Android 11, ni WEP ni mot de passe que le système refuserait (8 à 63 caractères ASCII
  imprimables). iOS : jamais, tant qu'il manque l'entitlement *Hotspot Configuration*.
  `rememberWifiNetworkJoiner()` (`expect` composable, appelé par la `Route`) ouvre l'écran des
  réglages `ACTION_WIFI_ADD_NETWORKS` : les réglages ne s'affichent qu'à une app qui attend leur
  résultat, d'où `rememberLauncherForActivityResult` et non un `startActivity` depuis le ViewModel.

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
| Aperçu et détection en direct | ✅ aperçu vérifié (émulateur) ; détection en direct à vérifier sur un appareil — CameraX + ML Kit Barcode Scanning | Livré, à vérifier sur un iPhone — `AVCaptureSession` + `AVCaptureMetadataOutput` | `expect` composable |
| Zoom, mise au point | Livré, à vérifier sur un appareil — `CameraControl` (`setZoomRatio`, `startFocusAndMetering`) | Livré, à vérifier sur un iPhone — `AVCaptureDevice` (`videoZoomFactor`, `focusPointOfInterest`) | `expect` composable |
| Torche | Livré, à vérifier sur un appareil (l'émulateur n'a pas de flash) — `CameraControl.enableTorch` | Livré, à vérifier sur un iPhone — `AVCaptureDevice.torchMode` | `expect` composable |
| Permission caméra | ✅ (refus, refus définitif, accord) — `RequestPermission` | ✅ demande et description affichées (simulateur) — `AVCaptureDevice.requestAccess` | `expect` composable |
| Détection dans une image | ✅ (émulateur : code lu, enregistré ; image sans code signalée) — Photo Picker + ML Kit (`InputImage`) | Livré, à vérifier sur un iPhone — `PHPickerViewController` + Vision (`VNDetectBarcodesRequest`) | `expect` composable |
| Ouvrir un lien, un mail, un SMS | ✅ (émulateur : lien ouvert dans Chrome, SMS prérempli dans Messages) — `LocalUriHandler` | ✅ lien et SMS (simulateur : Safari, message prérempli dans Messages) ; mail à vérifier sur un iPhone (pas de Mail sur le simulateur) — `LocalUriHandler`, `sms:` avec `&body=` | Compose |
| Vibration | `LocalHapticFeedback` (`Confirm`) | `LocalHapticFeedback` (`Confirm`) | Compose |
| Copier | ✅ (émulateur : confirmation du système, Android 13 et plus) — `LocalClipboard`, `ClipData` | ✅ (simulateur : texte dans le presse-papiers, message de l'app) — `LocalClipboard`, `ClipEntry.withPlainText` | Compose + `expect` pour créer le `ClipEntry` |
| Partager un texte | ✅ (émulateur : feuille de partage) — `ACTION_SEND` | ✅ (simulateur : feuille de partage) — `UIActivityViewController` | interface |
| Partager, enregistrer un QR code | `FileProvider` + `ACTION_SEND`, `MediaStore` | `UIActivityViewController`, Photos | interface |
| Ajouter un contact | ✅ (émulateur : Contacts prérempli, tous les champs) — `ACTION_INSERT_OR_EDIT` | ✅ (simulateur : contact enregistré, tous les champs) — `CNContactViewController` | interface |
| Rejoindre un réseau WiFi | ✅ (émulateur : réseau WPA2 enregistré par l'écran des réglages ; non proposé pour un mot de passe invalide) — `Settings.ACTION_WIFI_ADD_NETWORKS`, Android 11 et plus | Reporté (`NEHotspotConfigurationManager` exige l'entitlement *Hotspot Configuration*, donc un compte payant) : action non proposée | `expect` composable + interface |
| Ouvrir les réglages de l'app | ✅ `ACTION_APPLICATION_DETAILS_SETTINGS` | `UIApplicationOpenSettingsURLString` | dans l'état de permission caméra |
| Noter l'app | Lien Play Store | Lien App Store | interface |
| Version de l'app | `PackageManager` (`versionName`, `longVersionCode`) | `CFBundleShortVersionString`, `CFBundleVersion` | interface |